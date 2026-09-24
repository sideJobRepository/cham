package com.cham.collector.service;

import com.cham.collector.adapter.AdapterRegistry;
import com.cham.collector.adapter.BoardAdapter;
import com.cham.collector.config.CollectorProperties;
import com.cham.collector.domain.CollectSource;
import com.cham.collector.domain.Refs.AttachmentRef;
import com.cham.collector.domain.Refs.Downloaded;
import com.cham.collector.domain.Refs.PostRef;
import com.cham.collector.repository.CollectFileRepository;
import com.cham.collector.repository.CollectFileRepository.NewFile;
import com.cham.collector.repository.CollectFileRepository.ShaHit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.YearMonth;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * 기관 하나 수집: 목록 → 상세 → 첨부 다운로드 → S3 보관 → COLLECT_FILE 기록.
 *
 * 이미 받은 것은 다시 받지 않는다.
 *   - 정기 실행은 목록 1페이지만 본다
 *   - 이미 기록된 게시물은 상세를 열지 않는다 (수동 실행은 연다)
 *   - 같은 첨부(게시물+첨부키)는 받지 않는다
 *   - 내용이 같은 파일(해시)은 DUPLICATE 로만 적고 S3 에 다시 올리지 않는다
 * 게시물·첨부 하나가 실패해도 나머지는 계속한다. 실패는 작업 로그에 남는다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CollectService {

    private final AdapterRegistry adapters;
    private final CollectFileRepository fileRepository;
    private final S3Storage s3Storage;
    private final CollectorProperties props;

    public SourceResult collect(CollectSource source, CollectPlan plan, Long jobId) {
        SourceResult result = new SourceResult();

        Optional<BoardAdapter> found = adapters.find(source.engine());
        if (found.isEmpty()) {
            result.error = "이 게시판 엔진(" + source.engine() + ")은 수집기가 아직 지원하지 않습니다.";
            return result;
        }
        BoardAdapter adapter = found.get();

        if (plan.skipIfLastMonthExists()) {
            YearMonth lastMonth = YearMonth.now().minusMonths(1);
            if (!lastMonth.isBefore(plan.minPeriod()) && fileRepository.existsPeriod(source.id(), lastMonth)) {
                result.log(lastMonth + " 자료가 이미 있어 건너뜀");
                return result;
            }
        }

        YearMonth floor = plan.target() != null ? plan.target() : plan.minPeriod();
        boolean targetFound = false;

        for (int page = 1; page <= plan.pageLimit(); page++) {
            List<PostRef> posts;
            try {
                posts = adapter.listPosts(source, page);
            } catch (Exception e) {
                if (page == 1) {
                    result.error = "목록을 읽지 못했습니다: " + e.getMessage();
                    return result;
                }
                result.log(page + "페이지 목록 실패: " + e.getMessage());
                break;
            }
            if (posts.isEmpty()) {
                if (page == 1) result.log("목록에서 게시물을 찾지 못했습니다 (게시판 모양이 바뀌었을 수 있음)");
                break;
            }

            boolean anyAtOrAfterFloor = false;
            boolean passedFloor = false;
            for (PostRef post : posts) {
                result.postsSeen++;
                Optional<YearMonth> period = PeriodParser.parse(post.title(), null, post.postDate());

                if (period.isPresent()) {
                    if (!period.get().isBefore(floor)) anyAtOrAfterFloor = true;
                    else passedFloor = true;
                    if (period.get().isBefore(plan.minPeriod())) continue;
                    if (plan.target() != null && !period.get().equals(plan.target())) continue;
                } else if (plan.target() != null) {
                    continue;
                }
                if (plan.target() != null) targetFound = true;

                if (!plan.deepCheck() && fileRepository.existsPost(source.id(), post.postKey())) {
                    result.filesSkipped++;
                    continue;
                }
                collectPost(adapter, source, post, period.orElse(null), jobId, result);
            }

            // 목록은 최신순이다. 기준 달(지정한 달, 없으면 시작 달)보다 옛날 글이 나온 페이지에서 멈춘다.
            // 그 달을 찾았다고 바로 멈추지 않는다: 대전시의회처럼 한 달치가 두 페이지에 걸치는 곳이 있다
            if (passedFloor || !anyAtOrAfterFloor) break;
        }

        if (plan.target() != null && !targetFound) {
            result.log(plan.target() + " 게시물을 목록 " + plan.pageLimit() + "페이지 안에서 찾지 못했습니다 (아직 안 올렸을 수 있음)");
        }
        return result;
    }

    private void collectPost(BoardAdapter adapter, CollectSource source, PostRef post, YearMonth period,
                             Long jobId, SourceResult result) {
        List<AttachmentRef> attachments;
        try {
            attachments = adapter.attachments(source, post);
        } catch (Exception e) {
            result.filesFailed++;
            result.log("상세 실패 [" + post.title() + "]: " + e.getMessage());
            return;
        }

        List<AttachmentRef> wanted = attachments.stream()
                .filter(a -> source.allowExt().contains(extOf(a.displayName())))
                .toList();
        if (wanted.isEmpty()) {
            result.log("받을 첨부 없음 [" + post.title() + "]"
                    + (attachments.isEmpty() ? "" : " (첨부 " + attachments.size() + "개는 허용 형식 아님)"));
            return;
        }

        for (AttachmentRef attachment : wanted) {
            if (fileRepository.existsAttach(source.id(), post.postKey(), attachment.attachKey())) {
                result.filesSkipped++;
                continue;
            }
            try {
                saveAttachment(adapter, source, post, period, attachment, jobId, result);
            } catch (Exception e) {
                result.filesFailed++;
                result.log("첨부 실패 [" + attachment.displayName() + "]: " + e.getMessage());
                log.warn("첨부 실패 {} {}", source.code(), attachment.url(), e);
            }
        }
    }

    private void saveAttachment(BoardAdapter adapter, CollectSource source, PostRef post, YearMonth period,
                                AttachmentRef attachment, Long jobId, SourceResult result) throws Exception {
        Downloaded file = adapter.download(attachment);
        String originName = attachment.displayName() != null ? attachment.displayName() : file.headerFilename();
        if (originName == null) originName = post.postKey() + "." + extOf(attachment.url());
        String ext = extOf(originName);
        if (!source.allowExt().contains(ext) && file.headerFilename() != null) {
            ext = extOf(file.headerFilename());
        }

        // 제목에서 못 읽었으면 파일명에서 한 번 더 본다
        YearMonth filePeriod = period != null ? period
                : PeriodParser.parse(null, originName, post.postDate()).orElse(null);

        String sha = sha256(file.body());
        Optional<ShaHit> original = fileRepository.findOriginalBySha(sha);

        String status;
        String s3Key;
        Long dupOf = null;
        if (original.isPresent()) {
            status = "DUPLICATE";
            s3Key = original.get().s3Key();
            dupOf = original.get().fileId();
        } else {
            status = "COLLECTED";
            s3Key = s3Key(source, filePeriod, sha, ext);
            s3Storage.putIfAbsent(s3Key, file.body(), file.contentType());
        }

        try {
            fileRepository.insert(new NewFile(source.id(), jobId, filePeriod, post.postKey(), post.title(),
                    post.postDate(), post.detailUrl(), attachment.attachKey(), attachment.url(), originName, ext,
                    s3Key, sha, file.body().length, status, dupOf));
        } catch (DuplicateKeyException e) {
            // 동시에 돈 다른 수집기가 먼저 넣었다
            result.filesSkipped++;
            return;
        }

        if (original.isPresent()) {
            result.filesSkipped++;
            result.log("내용이 같은 파일이 이미 있음 [" + originName + "]");
        } else {
            result.filesNew++;
            result.log("새 파일 [" + originName + "] " + (filePeriod == null ? "(대상 월 모름)" : filePeriod));
        }
    }

    private String s3Key(CollectSource source, YearMonth period, String sha, String ext) {
        String folder = period == null ? "unknown"
                : String.format("%d/%02d", period.getYear(), period.getMonthValue());
        return props.s3Prefix() + "/" + source.code() + "/" + folder + "/" + sha + "." + ext;
    }

    static String extOf(String name) {
        if (name == null) return "";
        String n = name.replaceAll("[?#].*$", "").trim();
        int dot = n.lastIndexOf('.');
        return dot < 0 ? "" : n.substring(dot + 1).toLowerCase(Locale.ROOT).trim();
    }

    static String sha256(byte[] body) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(body));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
