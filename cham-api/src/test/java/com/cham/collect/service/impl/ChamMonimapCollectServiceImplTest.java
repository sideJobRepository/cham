package com.cham.collect.service.impl;

import com.cham.advice.exception.ExcelException;
import com.cham.caruse.CardUseInsertOptions;
import com.cham.caruse.CardUseRow;
import com.cham.caruse.repository.ChamMonimapCardUseRepository;
import com.cham.caruse.service.ChamMonimapCardUseService;
import com.cham.collect.dto.CollectImportRequest;
import com.cham.collect.dto.CollectJobRequest;
import com.cham.collect.dto.CollectPreviewResponse;
import com.cham.collect.entity.ChamMonimapCollectFile;
import com.cham.collect.entity.ChamMonimapCollectJob;
import com.cham.collect.entity.ChamMonimapCollectSource;
import com.cham.collect.enumeration.CollectFileStatus;
import com.cham.collect.enumeration.CollectJobStatus;
import com.cham.collect.repository.ChamMonimapCollectFileRepository;
import com.cham.collect.repository.ChamMonimapCollectJobRepository;
import com.cham.collect.repository.ChamMonimapCollectSourceRepository;
import com.cham.collect.service.ChamMonimapCollectService;
import com.cham.config.S3FileUtils;
import com.cham.dto.response.ApiResponse;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ChamMonimapCollectServiceImplTest {

    @Mock ChamMonimapCollectSourceRepository sourceRepository;
    @Mock ChamMonimapCollectFileRepository fileRepository;
    @Mock ChamMonimapCollectJobRepository jobRepository;
    @Mock ChamMonimapCardUseRepository cardUseRepository;
    @Mock ChamMonimapCardUseService cardUseService;
    @Mock S3FileUtils s3FileUtils;

    @InjectMocks ChamMonimapCollectServiceImpl service;

    private ChamMonimapCollectSource council;

    @BeforeEach
    void setUp() {
        council = source(1L, "중구의회", "기초의회", "대전 중구", null);
        when(cardUseRepository.findLatestNameByUser("대전 중구")).thenReturn(Map.of("의장", "오은규"));
    }

    @Test
    void 반영하면_비공개로_넣고_자동삭제키가_겹치면_번호를_붙인다() throws Exception {
        ChamMonimapCollectFile file = file(10L, council, CollectFileStatus.COLLECTED);
        when(fileRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(file));
        when(s3FileUtils.getBytes("k")).thenReturn(councilXlsx("2026-08-03 12:30"));
        when(cardUseRepository.existsByChamMonimapCardUseDelkey("수집-중구의회-2026-08")).thenReturn(true);
        when(cardUseService.insertRows(anyList(), anyString(), any())).thenReturn(2);

        ApiResponse response = service.importFile(10L, null, 7L);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<CardUseRow>> rows = ArgumentCaptor.forClass(List.class);
        verify(cardUseService).insertRows(rows.capture(), eq("수집-중구의회-2026-08-2"),
                eq(CardUseInsertOptions.COLLECT_IMPORT));
        assertThat(rows.getValue()).hasSize(2);
        // 의장은 기존 자료에서 이름을 찾고, 국장은 못 찾아 '공무원'
        assertThat(rows.getValue().get(0).name()).isEqualTo("오은규");
        assertThat(rows.getValue().get(0).ownerPosition()).isEqualTo("기초의회");
        assertThat(rows.getValue().get(1).name()).isEqualTo("공무원");

        assertThat(file.getChamMonimapCollectFileStatus()).isEqualTo(CollectFileStatus.IMPORTED);
        assertThat(file.getChamMonimapCollectFileDelkey()).isEqualTo("수집-중구의회-2026-08-2");
        assertThat(file.getChamMonimapCollectFileImportBy()).isEqualTo(7L);
        assertThat(response.getMessage()).contains("2건");
    }

    @Test
    void 화면에서_적은_이름으로_빈_이름을_채운다() throws Exception {
        ChamMonimapCollectFile file = file(10L, council, CollectFileStatus.COLLECTED);
        when(fileRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(file));
        when(s3FileUtils.getBytes("k")).thenReturn(councilXlsx("2026-08-03 12:30"));

        service.importFile(10L, new CollectImportRequest("내 삭제키", "사무국장"), 7L);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<CardUseRow>> rows = ArgumentCaptor.forClass(List.class);
        verify(cardUseService).insertRows(rows.capture(), eq("내 삭제키"), any());
        assertThat(rows.getValue().get(1).name()).isEqualTo("사무국장");
    }

    @Test
    void 날짜를_못읽은_줄이_있으면_반영하지_않는다() throws Exception {
        ChamMonimapCollectFile file = file(10L, council, CollectFileStatus.COLLECTED);
        when(fileRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(file));
        when(s3FileUtils.getBytes("k")).thenReturn(councilXlsx("추후기재"));

        assertThatThrownBy(() -> service.importFile(10L, null, 7L))
                .isInstanceOf(ExcelException.class)
                .hasMessageContaining("날짜를 읽지 못한 줄");
        verify(cardUseService, never()).insertRows(anyList(), anyString(), any());
        assertThat(file.getChamMonimapCollectFileStatus()).isEqualTo(CollectFileStatus.COLLECTED);
    }

    @Test
    void 검수대기가_아니면_반영하지_않는다() {
        when(fileRepository.findByIdForUpdate(10L))
                .thenReturn(Optional.of(file(10L, council, CollectFileStatus.IMPORTED)));

        assertThatThrownBy(() -> service.importFile(10L, null, 7L))
                .isInstanceOf(ExcelException.class)
                .hasMessageContaining("COLLECTED");
    }

    @Test
    void 직접_적은_삭제키가_겹치면_막는다() throws Exception {
        when(fileRepository.findByIdForUpdate(10L))
                .thenReturn(Optional.of(file(10L, council, CollectFileStatus.COLLECTED)));
        when(s3FileUtils.getBytes("k")).thenReturn(councilXlsx("2026-08-03 12:30"));
        when(cardUseRepository.existsByChamMonimapCardUseDelkey("대전08")).thenReturn(true);

        assertThatThrownBy(() -> service.importFile(10L, new CollectImportRequest("대전08", null), 7L))
                .hasMessage("이미 존재하는 삭제키입니다.");
    }

    @Test
    void 미리보기는_이름을_채우고_자리값을_보여준다() throws Exception {
        when(fileRepository.findWithSource(10L))
                .thenReturn(Optional.of(file(10L, council, CollectFileStatus.COLLECTED)));
        when(s3FileUtils.getBytes("k")).thenReturn(councilXlsx("2026-08-03 12:30"));

        CollectPreviewResponse preview = service.preview(10L, 50, null);

        assertThat(preview.totalRows()).isEqualTo(2);
        assertThat(preview.blockingRows()).isZero();
        assertThat(preview.noNameRows()).isEqualTo(1);
        assertThat(preview.suggestedDeleteKey()).isEqualTo("수집-중구의회-2026-08");
        assertThat(preview.rows().get(0).name()).isEqualTo("오은규");
        assertThat(preview.rows().get(1).name()).isEqualTo("공무원");
        assertThat(preview.rows().get(1).warnings()).isEmpty();
    }

    @Test
    void 업로드_양식으로_바꾸면_수동업로드가_그대로_읽는다() throws Exception {
        when(fileRepository.findWithSource(10L))
                .thenReturn(Optional.of(file(10L, council, CollectFileStatus.COLLECTED)));
        when(s3FileUtils.getBytes("k")).thenReturn(councilXlsx("2026-08-03 12:30"));

        ChamMonimapCollectService.DownloadFile form = service.uploadForm(10L, null);

        assertThat(form.fileName()).isEqualTo("수집-중구의회-2026-08 업로드양식.xlsx");
        try (org.apache.poi.ss.usermodel.Workbook wb =
                     org.apache.poi.ss.usermodel.WorkbookFactory.create(new java.io.ByteArrayInputStream(form.body()))) {
            Sheet sheet = wb.getSheetAt(0);
            Row first = sheet.getRow(1);
            // ExcelColumns 순서: 0 기관/직책 1 지역 2 사용자 3 이름 4 일자 5 시간 6 장소 7 주소 … 13 삭제키
            assertThat(first.getCell(0).getStringCellValue()).isEqualTo("기초의회");
            assertThat(first.getCell(2).getStringCellValue()).isEqualTo("의장");
            assertThat(first.getCell(3).getStringCellValue()).isEqualTo("오은규");
            assertThat(first.getCell(4).getStringCellValue()).isEqualTo("2026-08-03");
            assertThat(first.getCell(5).getStringCellValue()).isEqualTo("12:30");
            assertThat(first.getCell(6).getStringCellValue()).isEqualTo("스시무희");
            assertThat(first.getCell(13).getStringCellValue()).isEqualTo("수집-중구의회-2026-08");
            assertThat(sheet.getRow(2).getCell(3).getStringCellValue()).isEqualTo("공무원");
        }
    }

    @Test
    void 반영한_자료가_남아있으면_되돌리지_않는다() {
        ChamMonimapCollectFile file = file(10L, council, CollectFileStatus.COLLECTED);
        file.markImported("수집-중구의회-2026-08", 7L);
        when(fileRepository.findById(10L)).thenReturn(Optional.of(file));
        when(cardUseRepository.existsByChamMonimapCardUseDelkey("수집-중구의회-2026-08")).thenReturn(true);

        assertThatThrownBy(() -> service.resetFile(10L)).hasMessageContaining("먼저 삭제키로 지운 뒤");

        when(cardUseRepository.existsByChamMonimapCardUseDelkey("수집-중구의회-2026-08")).thenReturn(false);
        service.resetFile(10L);
        assertThat(file.getChamMonimapCollectFileStatus()).isEqualTo(CollectFileStatus.COLLECTED);
        assertThat(file.getChamMonimapCollectFileDelkey()).isNull();
    }

    @Test
    void 달을_골라_수집을_요청한다() {
        when(sourceRepository.findById(1L)).thenReturn(Optional.of(council));

        ApiResponse response = service.requestJob(new CollectJobRequest(1L, 2026, 8), 7L);

        ArgumentCaptor<ChamMonimapCollectJob> job = ArgumentCaptor.forClass(ChamMonimapCollectJob.class);
        verify(jobRepository).save(job.capture());
        assertThat(job.getValue().getChamMonimapCollectJobStatus()).isEqualTo(CollectJobStatus.REQUESTED);
        assertThat(job.getValue().getChamMonimapCollectJobTargetYear()).isEqualTo(2026);
        assertThat(job.getValue().getChamMonimapCollectJobTargetMonth()).isEqualTo(8);
        assertThat(job.getValue().getCollectSource()).isSameAs(council);
        assertThat(response.getMessage()).contains("중구의회 2026년 8월분");
    }

    @Test
    void 수집_요청_검증() {
        assertThatThrownBy(() -> service.requestJob(new CollectJobRequest(null, 2026, null), 7L))
                .hasMessageContaining("같이");
        assertThatThrownBy(() -> service.requestJob(new CollectJobRequest(null, 2026, 6), 7L))
                .hasMessageContaining("2026년 7월부터");

        when(jobRepository.existsActive()).thenReturn(true);
        assertThatThrownBy(() -> service.requestJob(new CollectJobRequest(null, null, null), 7L))
                .hasMessageContaining("이미 대기 중이거나");
        verify(jobRepository, never()).save(any());
    }

    // ── 픽스처 ──

    private static ChamMonimapCollectSource source(Long id, String name, String position, String region, String defaultName) {
        ChamMonimapCollectSource s = new ChamMonimapCollectSource();
        ReflectionTestUtils.setField(s, "chamMonimapCollectSourceId", id);
        ReflectionTestUtils.setField(s, "chamMonimapCollectSourceName", name);
        ReflectionTestUtils.setField(s, "chamMonimapCollectSourcePositionName", position);
        ReflectionTestUtils.setField(s, "chamMonimapCollectSourceRegion", region);
        ReflectionTestUtils.setField(s, "chamMonimapCollectSourceDefaultName", defaultName);
        return s;
    }

    private static ChamMonimapCollectFile file(Long id, ChamMonimapCollectSource source, CollectFileStatus status) {
        ChamMonimapCollectFile f = new ChamMonimapCollectFile();
        ReflectionTestUtils.setField(f, "chamMonimapCollectFileId", id);
        ReflectionTestUtils.setField(f, "collectSource", source);
        ReflectionTestUtils.setField(f, "chamMonimapCollectFileYear", 2026);
        ReflectionTestUtils.setField(f, "chamMonimapCollectFileMonth", 8);
        ReflectionTestUtils.setField(f, "chamMonimapCollectFileExt", "xlsx");
        ReflectionTestUtils.setField(f, "chamMonimapCollectFileS3Key", "k");
        ReflectionTestUtils.setField(f, "chamMonimapCollectFileOriginName", "업무추진비 공개 2608.xlsx");
        ReflectionTestUtils.setField(f, "chamMonimapCollectFileStatus", status);
        return f;
    }

    private static byte[] councilXlsx(String firstDate) throws Exception {
        try (XSSFWorkbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet s = wb.createSheet("⊙");
            row(s, 0, "연번", "사용일시", "사용자", "사용장소(가맹점명)", "가맹점 주소", "사용목적", "대상인원(명)", "사용금액(원)", "사용방법");
            row(s, 1, "1", firstDate, "의장", "스시무희", "대전 중구 중앙로112번길 24", "간담회", "4", "120000", "카드");
            row(s, 2, "2", "2026-08-05 19:00", "국장", "칼국수집", "대전 중구 대종로 1", "격려", "10", "55000", "카드");
            wb.write(out);
            return out.toByteArray();
        }
    }

    private static void row(Sheet sheet, int idx, String... values) {
        Row row = sheet.createRow(idx);
        for (int i = 0; i < values.length; i++) {
            row.createCell(i).setCellValue(values[i]);
        }
    }
}
