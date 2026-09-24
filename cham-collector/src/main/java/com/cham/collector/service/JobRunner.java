package com.cham.collector.service;

import com.cham.collector.config.CollectorProperties;
import com.cham.collector.domain.CollectJob;
import com.cham.collector.domain.CollectSource;
import com.cham.collector.notify.Notifier;
import com.cham.collector.repository.CollectJobRepository;
import com.cham.collector.repository.CollectSourceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/** 작업 하나 실행: 대상 기관을 차례로 돌고 결과를 COLLECT_JOB 에 적는다 */
@Slf4j
@Service
@RequiredArgsConstructor
public class JobRunner {

    private static final int MAX_LOG = 60_000;
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm:ss");

    private final CollectSourceRepository sourceRepository;
    private final CollectJobRepository jobRepository;
    private final CollectService collectService;
    private final CollectorProperties props;
    private final Notifier notifier;

    public void run(CollectJob job) {
        StringBuilder logText = new StringBuilder();
        int posts = 0, fresh = 0, skipped = 0, failed = 0;
        List<String> failedSources = new ArrayList<>();
        int succeeded = 0;

        try {
            List<CollectSource> sources = targets(job);
            CollectPlan plan = plan(job);
            append(logText, "시작: " + (job.scheduled() ? "정기" : "수동")
                    + (plan.target() != null ? " / " + plan.target() + "분" : "")
                    + " / 기관 " + sources.size() + "곳 / 목록 " + plan.pageLimit() + "페이지까지");

            for (CollectSource source : sources) {
                SourceResult r;
                try {
                    r = collectService.collect(source, plan, job.id());
                } catch (Exception e) {
                    log.error("수집 실패 {}", source.code(), e);
                    r = new SourceResult();
                    r.error = e.getClass().getSimpleName() + ": " + e.getMessage();
                }

                posts += r.postsSeen();
                fresh += r.filesNew();
                skipped += r.filesSkipped();
                failed += r.filesFailed();

                append(logText, "[" + source.name() + "] 게시물 " + r.postsSeen() + " / 새 파일 " + r.filesNew()
                        + " / 건너뜀 " + r.filesSkipped() + " / 실패 " + r.filesFailed());
                r.lines().forEach(line -> append(logText, "  - " + line));

                if (r.failed()) {
                    append(logText, "  ! " + r.error());
                    failedSources.add(source.name());
                    sourceRepository.markError(source.id(), r.error());
                    notifier.sourceFailed(source, r.error());
                } else {
                    succeeded++;
                    sourceRepository.markSuccess(source.id());
                }
                jobRepository.updateProgress(job.id(), posts, fresh, skipped, failed, cut(logText));
            }

            boolean ok = sources.isEmpty() || succeeded > 0;
            append(logText, "끝: 새 파일 " + fresh + "개" + (failedSources.isEmpty() ? "" : " / 실패 기관 " + failedSources));
            jobRepository.updateProgress(job.id(), posts, fresh, skipped, failed, cut(logText));
            jobRepository.finish(job.id(), ok ? "DONE" : "FAILED");
            if (!failedSources.isEmpty()) notifier.jobFinishedWithFailures(job.id(), failedSources);
        } catch (Exception e) {
            log.error("작업 {} 실패", job.id(), e);
            append(logText, "작업 실패: " + e.getMessage());
            jobRepository.updateProgress(job.id(), posts, fresh, skipped, failed, cut(logText));
            jobRepository.finish(job.id(), "FAILED");
        }
    }

    private List<CollectSource> targets(CollectJob job) {
        if (job.sourceId() == null) return sourceRepository.findEnabled();
        return sourceRepository.findById(job.sourceId()).map(List::of).orElse(List.of());
    }

    private CollectPlan plan(CollectJob job) {
        if (job.scheduled()) {
            // 직전 달 자료가 있어도 목록 1페이지는 본다. 대전시의회처럼 한 달에 게시물이 여러 건(위원회별)
            // 나눠 올라오는 곳이 있어서다. 이미 받은 게시물은 상세도 안 열어서 기관당 하루 요청 1번이다
            return new CollectPlan(props.scheduledPageLimit(), props.minYearMonth(), null, false, false);
        }
        if (job.target() != null) {
            return new CollectPlan(props.targetPageLimit(), props.minYearMonth(), job.target(), false, true);
        }
        int pages = job.pageLimit() != null ? job.pageLimit() : props.manualPageLimit();
        return new CollectPlan(pages, props.minYearMonth(), null, false, true);
    }

    private static void append(StringBuilder sb, String line) {
        sb.append(LocalDateTime.now().format(TIME)).append(' ').append(line).append('\n');
    }

    private static String cut(StringBuilder sb) {
        return sb.length() <= MAX_LOG ? sb.toString() : sb.substring(0, MAX_LOG) + "\n…(생략)";
    }
}
