package com.cham.collect.service.impl;

import com.cham.advice.exception.ExcelException;
import com.cham.caruse.CardUseDefaults;
import com.cham.caruse.CardUseInsertOptions;
import com.cham.caruse.CardUseRow;
import com.cham.caruse.repository.ChamMonimapCardUseRepository;
import com.cham.caruse.service.ChamMonimapCardUseService;
import com.cham.collect.CollectRules;
import com.cham.collect.dto.*;
import com.cham.collect.entity.ChamMonimapCollectFile;
import com.cham.collect.entity.ChamMonimapCollectJob;
import com.cham.collect.entity.ChamMonimapCollectSource;
import com.cham.collect.enumeration.CollectFileStatus;
import com.cham.collect.parser.HeaderMappedSheetParser;
import com.cham.collect.parser.HeaderMappedSheetParser.ParseResult;
import com.cham.collect.parser.HeaderMappedSheetParser.ParsedRow;
import com.cham.collect.parser.SourceDefaults;
import com.cham.collect.repository.ChamMonimapCollectFileRepository;
import com.cham.collect.repository.ChamMonimapCollectJobRepository;
import com.cham.collect.repository.ChamMonimapCollectSourceRepository;
import com.cham.collect.service.ChamMonimapCollectService;
import com.cham.config.S3FileUtils;
import com.cham.dto.response.ApiResponse;
import com.cham.page.PageResponse;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChamMonimapCollectServiceImpl implements ChamMonimapCollectService {

    public static final String WARN_NO_NAME = "이름 없음";

    // 월 표 한 칸에 파일이 여럿이면 관리자가 손댈 것을 먼저 보여준다
    private static final List<CollectFileStatus> CELL_PRIORITY = List.of(
            CollectFileStatus.COLLECTED, CollectFileStatus.FAILED,
            CollectFileStatus.IMPORTED, CollectFileStatus.IGNORED);

    private final ChamMonimapCollectSourceRepository sourceRepository;
    private final ChamMonimapCollectFileRepository fileRepository;
    private final ChamMonimapCollectJobRepository jobRepository;
    private final ChamMonimapCardUseRepository cardUseRepository;
    private final ChamMonimapCardUseService cardUseService;
    private final S3FileUtils s3FileUtils;

    private final HeaderMappedSheetParser parser = new HeaderMappedSheetParser();

    @Override
    public List<CollectSourceResponse> selectSources() {
        return sources().stream()
                .map(s -> new CollectSourceResponse(
                        s.getChamMonimapCollectSourceId(),
                        s.getChamMonimapCollectSourceCode(),
                        s.getChamMonimapCollectSourceName(),
                        s.getChamMonimapCollectSourceEngine(),
                        s.getChamMonimapCollectSourceListUrl(),
                        s.getChamMonimapCollectSourcePositionName(),
                        s.getChamMonimapCollectSourceRegion(),
                        s.getChamMonimapCollectSourceDefaultName(),
                        s.getChamMonimapCollectSourceFileFormat(),
                        s.getChamMonimapCollectSourceEnabled(),
                        s.getChamMonimapCollectSourceLastSuccess(),
                        s.getChamMonimapCollectSourceLastError(),
                        s.getChamMonimapCollectSourceNote()))
                .toList();
    }

    @Override
    public CollectMonthMatrixResponse selectMonthMatrix(int year) {
        Map<Long, Map<Integer, List<CollectFileCell>>> bySourceMonth = fileRepository.findYearCells(year).stream()
                .collect(Collectors.groupingBy(CollectFileCell::sourceId,
                        Collectors.groupingBy(CollectFileCell::month)));

        List<CollectMonthMatrixResponse.Row> rows = new ArrayList<>();
        for (ChamMonimapCollectSource source : sources()) {
            Map<Integer, List<CollectFileCell>> byMonth =
                    bySourceMonth.getOrDefault(source.getChamMonimapCollectSourceId(), Map.of());

            List<CollectMonthMatrixResponse.Cell> cells = new ArrayList<>();
            for (int month = 1; month <= 12; month++) {
                List<CollectFileCell> files = byMonth.getOrDefault(month, List.of());
                CollectFileCell top = files.stream()
                        .min(Comparator.comparingInt(f -> CELL_PRIORITY.indexOf(f.status())))
                        .orElse(null);
                cells.add(new CollectMonthMatrixResponse.Cell(month,
                        top == null ? null : top.fileId(),
                        top == null ? null : top.status(),
                        files.size()));
            }
            rows.add(new CollectMonthMatrixResponse.Row(
                    source.getChamMonimapCollectSourceId(),
                    source.getChamMonimapCollectSourceName(),
                    source.getChamMonimapCollectSourceEnabled(),
                    source.getChamMonimapCollectSourceFileFormat(),
                    source.getChamMonimapCollectSourceLastError(),
                    cells));
        }
        return new CollectMonthMatrixResponse(year, CollectRules.START.getYear(),
                CollectRules.START.getMonthValue(), rows);
    }

    @Override
    public PageResponse<CollectFileResponse> selectFiles(Long sourceId, Integer year, Integer month,
                                                         CollectFileStatus status, Pageable pageable) {
        return PageResponse.from(fileRepository.findFiles(sourceId, year, month, status, pageable));
    }

    @Override
    public CollectPreviewResponse preview(Long fileId, int limit, String defaultName) {
        ChamMonimapCollectFile file = fileRepository.findWithSource(fileId)
                .orElseThrow(() -> new ExcelException("존재하지 않는 수집 파일입니다.", 400));
        ChamMonimapCollectSource source = file.getCollectSource();

        ParseResult parsed = parse(file);
        List<ParsedRow> rows = fillNames(parsed.rows(), source, defaultName);

        String suggestedKey = file.getChamMonimapCollectFileDelkey() != null
                ? file.getChamMonimapCollectFileDelkey()
                : availableDeleteKey(CollectRules.deleteKey(source.getChamMonimapCollectSourceName(),
                file.getChamMonimapCollectFileYear(), file.getChamMonimapCollectFileMonth()));

        boolean placeMissing = !rows.isEmpty() && rows.stream().allMatch(r -> isBlank(r.row().addrName()));

        return new CollectPreviewResponse(
                file.getChamMonimapCollectFileId(),
                source.getChamMonimapCollectSourceName(),
                file.getChamMonimapCollectFileYear(),
                file.getChamMonimapCollectFileMonth(),
                suggestedKey,
                source.getChamMonimapCollectSourceDefaultName(),
                placeMissing,
                rows.size(),
                rows.stream().filter(r -> !r.warnings().isEmpty()).count(),
                rows.stream().filter(ParsedRow::blocking).count(),
                rows.stream().filter(r -> isBlank(r.row().name())).count(),
                parsed.sheets(),
                parsed.fileWarnings(),
                rows.stream().limit(Math.max(1, limit)).map(this::toPreviewRow).toList());
    }

    @Override
    @Transactional
    public ApiResponse importFile(Long fileId, CollectImportRequest request, Long memberId) {
        ChamMonimapCollectFile file = fileRepository.findByIdForUpdate(fileId)
                .orElseThrow(() -> new ExcelException("존재하지 않는 수집 파일입니다.", 400));
        if (file.getChamMonimapCollectFileStatus() != CollectFileStatus.COLLECTED) {
            throw new ExcelException("검수 대기(COLLECTED) 상태인 파일만 반영할 수 있습니다. 지금 상태: "
                    + file.getChamMonimapCollectFileStatus(), 400);
        }
        ChamMonimapCollectSource source = file.getCollectSource();

        ParseResult parsed = parse(file);
        List<ParsedRow> rows = fillNames(parsed.rows(), source, request == null ? null : request.defaultName());
        if (rows.isEmpty()) {
            throw new ExcelException("반영할 줄이 없습니다. " + String.join(" ", parsed.fileWarnings()), 400);
        }
        List<String> blocking = rows.stream()
                .filter(ParsedRow::blocking)
                .map(r -> r.row().sheetName() + " " + r.row().sourceRowNum() + "행")
                .toList();
        if (!blocking.isEmpty()) {
            throw new ExcelException("날짜를 읽지 못한 줄이 있어 반영할 수 없습니다: "
                    + String.join(", ", blocking.subList(0, Math.min(10, blocking.size())))
                    + (blocking.size() > 10 ? " 외 " + (blocking.size() - 10) + "줄" : ""), 400);
        }

        String deleteKey;
        String requested = request == null ? null : trim(request.deleteKey());
        if (StringUtils.hasText(requested)) {
            // 직접 적은 삭제키가 겹치면 다른 업로드와 섞이므로 붙이지 않고 막는다
            if (cardUseRepository.existsByChamMonimapCardUseDelkey(requested)) {
                throw new ExcelException("이미 존재하는 삭제키입니다.", 400);
            }
            deleteKey = requested;
        } else {
            deleteKey = availableDeleteKey(CollectRules.deleteKey(source.getChamMonimapCollectSourceName(),
                    file.getChamMonimapCollectFileYear(), file.getChamMonimapCollectFileMonth()));
        }

        int inserted = cardUseService.insertRows(
                rows.stream().map(ParsedRow::row).toList(), deleteKey, CardUseInsertOptions.COLLECT_IMPORT);
        file.markImported(deleteKey, memberId);

        return new ApiResponse(200, true,
                "비공개로 " + inserted + "건 반영했습니다. 공개관리에서 '" + deleteKey + "' 를 확인하고 공개로 돌려주세요.");
    }

    @Override
    @Transactional
    public ApiResponse ignoreFile(Long fileId) {
        ChamMonimapCollectFile file = findFile(fileId);
        CollectFileStatus status = file.getChamMonimapCollectFileStatus();
        if (status != CollectFileStatus.COLLECTED && status != CollectFileStatus.FAILED) {
            throw new ExcelException("검수 대기나 실패 상태만 무시할 수 있습니다. 지금 상태: " + status, 400);
        }
        file.markIgnored();
        return new ApiResponse(200, true, "무시했습니다.");
    }

    @Override
    @Transactional
    public ApiResponse resetFile(Long fileId) {
        ChamMonimapCollectFile file = findFile(fileId);
        CollectFileStatus status = file.getChamMonimapCollectFileStatus();
        switch (status) {
            case COLLECTED -> throw new ExcelException("이미 검수 대기 상태입니다.", 400);
            case DUPLICATE -> throw new ExcelException("중복 파일은 되돌릴 수 없습니다. 원본 파일을 쓰세요.", 400);
            case IMPORTED -> {
                String key = file.getChamMonimapCollectFileDelkey();
                if (key != null && cardUseRepository.existsByChamMonimapCardUseDelkey(key)) {
                    throw new ExcelException("반영한 자료('" + key + "')가 남아 있습니다. 먼저 삭제키로 지운 뒤 되돌리세요.", 400);
                }
            }
            default -> {
            }
        }
        file.reset();
        return new ApiResponse(200, true, "검수 대기로 되돌렸습니다.");
    }

    @Override
    public DownloadFile download(Long fileId) {
        ChamMonimapCollectFile file = findFile(fileId);
        return new DownloadFile(file.getChamMonimapCollectFileOriginName(),
                s3FileUtils.getBytes(file.getChamMonimapCollectFileS3Key()));
    }

    @Override
    public PageResponse<CollectJobResponse> selectJobs(Pageable pageable) {
        return PageResponse.from(jobRepository.findJobs(pageable));
    }

    @Override
    @Transactional
    public ApiResponse requestJob(CollectJobRequest request, Long memberId) {
        Integer year = request == null ? null : request.year();
        Integer month = request == null ? null : request.month();
        if ((year == null) != (month == null)) {
            throw new ExcelException("연도와 월은 같이 고르거나 같이 비워 주세요.", 400);
        }
        if (year != null) {
            if (month < 1 || month > 12) {
                throw new ExcelException("월이 올바르지 않습니다.", 400);
            }
            if (!CollectRules.isCollectable(year, month)) {
                throw new ExcelException(String.format("%d년 %d월부터 이번 달까지만 수집할 수 있습니다.",
                        CollectRules.START.getYear(), CollectRules.START.getMonthValue()), 400);
            }
        }

        ChamMonimapCollectSource source = null;
        if (request != null && request.sourceId() != null) {
            source = sourceRepository.findById(request.sourceId())
                    .orElseThrow(() -> new ExcelException("존재하지 않는 기관입니다.", 400));
        }
        if (jobRepository.existsActive()) {
            throw new ExcelException("이미 대기 중이거나 실행 중인 수집이 있습니다. 끝난 뒤 다시 눌러 주세요.", 400);
        }

        jobRepository.save(ChamMonimapCollectJob.manual(memberId, source, year, month));

        String target = (source == null ? "전체 기관" : source.getChamMonimapCollectSourceName())
                + (year == null ? "" : String.format(" %d년 %d월분", year, month));
        return new ApiResponse(200, true, target + " 수집을 요청했습니다. 1분 안에 시작합니다.");
    }

    // ── 내부 ─────────────────────────────────────────────────

    private List<ChamMonimapCollectSource> sources() {
        return sourceRepository.findAllByOrderByChamMonimapCollectSourceSortAscChamMonimapCollectSourceIdAsc();
    }

    private ChamMonimapCollectFile findFile(Long fileId) {
        return fileRepository.findById(fileId)
                .orElseThrow(() -> new ExcelException("존재하지 않는 수집 파일입니다.", 400));
    }

    private ParseResult parse(ChamMonimapCollectFile file) {
        String ext = file.getChamMonimapCollectFileExt() == null ? "" : file.getChamMonimapCollectFileExt().toLowerCase();
        if (!ext.equals("xlsx") && !ext.equals("xls")) {
            throw new ExcelException("엑셀(xlsx/xls) 파일만 미리보기·반영할 수 있습니다. 이 파일: " + ext, 400);
        }
        ChamMonimapCollectSource source = file.getCollectSource();
        SourceDefaults defaults = new SourceDefaults(
                source.getChamMonimapCollectSourcePositionName(),
                source.getChamMonimapCollectSourceRegion(),
                source.getChamMonimapCollectSourceDefaultUser(),
                source.getChamMonimapCollectSourceDefaultName(),
                file.getChamMonimapCollectFileYear(),
                file.getChamMonimapCollectFileMonth());

        byte[] bytes = s3FileUtils.getBytes(file.getChamMonimapCollectFileS3Key());
        try (Workbook workbook = WorkbookFactory.create(new ByteArrayInputStream(bytes))) {
            return parser.parse(workbook, defaults);
        } catch (IOException | RuntimeException e) {
            if (e instanceof ExcelException ee) throw ee;
            throw new ExcelException("엑셀을 열 수 없습니다: " + e.getMessage(), 400);
        }
    }

    /**
     * 이름이 빈 줄을 채운다. 원본에 사람 이름이 없는 의회 자료가 대상이다.
     * 1) 같은 지역에서 같은 사용자(직함)가 가장 최근에 쓴 이름 ('의장' → '오은규')
     * 2) 화면에서 적은 이름
     * 못 채우면 '이름 없음' 경고만 붙이고 그대로 둔다
     */
    private List<ParsedRow> fillNames(List<ParsedRow> rows, ChamMonimapCollectSource source, String requestName) {
        if (rows.stream().noneMatch(r -> isBlank(r.row().name()))) return rows;

        Map<String, String> latest = cardUseRepository.findLatestNameByUser(source.getChamMonimapCollectSourceRegion());
        String fallback = trim(requestName);

        List<ParsedRow> result = new ArrayList<>(rows.size());
        for (ParsedRow r : rows) {
            if (!isBlank(r.row().name())) {
                result.add(r);
                continue;
            }
            String user = trim(r.row().user());
            String name = user == null ? null : latest.get(user);
            if (isBlank(name)) name = fallback;

            List<String> warnings = r.warnings();
            if (isBlank(name)) {
                warnings = new ArrayList<>(warnings);
                warnings.add(WARN_NO_NAME);
            }
            CardUseRow c = r.row();
            result.add(new ParsedRow(new CardUseRow(c.ownerPosition(), c.region(), c.user(), name, c.date(), c.time(),
                    c.addrName(), c.addrDetail(), c.purpose(), c.personnel(), c.amount(), c.method(), c.remark(),
                    c.sourceRowNum(), c.sheetName()), warnings, r.blocking()));
        }
        return result;
    }

    // 자동 삭제키가 이미 있으면 -2, -3 을 붙인다
    private String availableDeleteKey(String base) {
        if (!cardUseRepository.existsByChamMonimapCardUseDelkey(base)) return base;
        for (int i = 2; i < 100; i++) {
            String candidate = base + "-" + i;
            if (!cardUseRepository.existsByChamMonimapCardUseDelkey(candidate)) return candidate;
        }
        throw new ExcelException("쓸 수 있는 삭제키를 찾지 못했습니다. 직접 적어 주세요.", 400);
    }

    private CollectPreviewResponse.Row toPreviewRow(ParsedRow r) {
        CardUseRow c = r.row();
        return new CollectPreviewResponse.Row(c.sheetName(), c.sourceRowNum(), c.ownerPosition(), c.region(),
                c.user(), c.name(), c.date(), c.time(),
                // 저장할 때 들어갈 자리값을 그대로 보여준다. 경고에 '→ 자리값' 이 같이 붙어 있다
                isBlank(c.addrName()) ? CardUseDefaults.ADDR_NAME : c.addrName(),
                isBlank(c.addrDetail()) ? CardUseDefaults.DETAIL_ADDR : c.addrDetail(),
                c.purpose(), c.personnel(), c.amount(), c.method(), c.remark(), r.warnings(), r.blocking());
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    private static String trim(String s) {
        return s == null ? null : s.trim();
    }
}
