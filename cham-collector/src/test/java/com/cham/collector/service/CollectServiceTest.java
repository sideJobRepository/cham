package com.cham.collector.service;

import com.cham.collector.adapter.AdapterRegistry;
import com.cham.collector.adapter.BoardAdapter;
import com.cham.collector.config.CollectorProperties;
import com.cham.collector.domain.CollectSource;
import com.cham.collector.domain.EngineType;
import com.cham.collector.domain.Refs.AttachmentRef;
import com.cham.collector.domain.Refs.Downloaded;
import com.cham.collector.domain.Refs.PostRef;
import com.cham.collector.repository.CollectFileRepository;
import com.cham.collector.repository.CollectFileRepository.NewFile;
import com.cham.collector.repository.CollectFileRepository.ShaHit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class CollectServiceTest {

    private static final YearMonth START = YearMonth.of(2026, 8);

    private final BoardAdapter adapter = mock(BoardAdapter.class);
    private final CollectFileRepository files = mock(CollectFileRepository.class);
    private final S3Storage s3 = mock(S3Storage.class);
    private CollectService service;

    private final CollectSource source = new CollectSource(8L, "JUNGGU_COUNCIL", "중구의회", EngineType.COUNCIL_B,
            "https://x/list", "https://x/view?uid={postKey}", null, "page", null, Set.of("xlsx", "xls"), true);

    private final PostRef aug = new PostRef("A", "2026.8월 중구의회 업무추진비", null, "https://x/view?uid=A");
    private final PostRef jul = new PostRef("B", "2026.7월 중구의회 업무추진비", null, "https://x/view?uid=B");

    @BeforeEach
    void setUp() throws Exception {
        when(adapter.engine()).thenReturn(EngineType.COUNCIL_B);
        CollectorProperties props = new CollectorProperties("0 0 3 * * *", 60000, "2026-08", 0, 1000, 1000, 0,
                1_000_000, 1, 2, 5, "test", "collect", "", "");
        service = new CollectService(new AdapterRegistry(List.of(adapter)), files, s3, props);

        when(adapter.listPosts(any(), eq(1))).thenReturn(List.of(aug, jul));
        when(adapter.attachments(any(), eq(aug))).thenReturn(List.of(
                new AttachmentRef("F1", "https://x/down?uid=F1", "업무추진비 공개 2608.xlsx", aug.detailUrl()),
                new AttachmentRef("F2", "https://x/down?uid=F2", "안내문.hwp", aug.detailUrl())));
        when(adapter.download(any())).thenReturn(new Downloaded(new byte[]{1, 2, 3}, null, "application/vnd.ms-excel"));
    }

    @Test
    void 새_파일을_받아_S3에_올리고_기록한다_8월_이전은_받지_않는다() throws Exception {
        CollectPlan plan = new CollectPlan(1, START, null, false, false);

        SourceResult r = service.collect(source, plan, 99L);

        assertThat(r.filesNew()).isEqualTo(1);
        assertThat(r.failed()).isFalse();
        verify(adapter, never()).attachments(any(), eq(jul));          // 7월분은 시작 전
        verify(adapter, times(1)).download(any());                    // hwp 는 안 받는다

        ArgumentCaptor<NewFile> saved = ArgumentCaptor.forClass(NewFile.class);
        verify(files).insert(saved.capture());
        assertThat(saved.getValue().period()).isEqualTo(YearMonth.of(2026, 8));
        assertThat(saved.getValue().status()).isEqualTo("COLLECTED");
        assertThat(saved.getValue().s3Key()).startsWith("collect/JUNGGU_COUNCIL/2026/08/").endsWith(".xlsx");
        assertThat(saved.getValue().jobId()).isEqualTo(99L);
        verify(s3).putIfAbsent(eq(saved.getValue().s3Key()), any(), any());
    }

    @Test
    void 정기_실행은_직전_달이_있으면_목록도_열지_않는다() throws Exception {
        when(files.existsPeriod(eq(8L), any())).thenReturn(true);

        SourceResult r = service.collect(source, new CollectPlan(1, START, null, true, false), 1L);

        assertThat(r.lines()).anyMatch(l -> l.contains("이미 있어 건너뜀"));
        verify(adapter, never()).listPosts(any(), anyInt());
    }

    @Test
    void 이미_기록된_게시물은_상세를_열지_않는다() throws Exception {
        when(files.existsPost(8L, "A")).thenReturn(true);

        SourceResult r = service.collect(source, new CollectPlan(1, START, null, false, false), 1L);

        assertThat(r.filesSkipped()).isEqualTo(1);
        verify(adapter, never()).attachments(any(), any());
    }

    @Test
    void 내용이_같은_파일은_중복으로만_적는다() throws Exception {
        when(files.findOriginalBySha(anyString())).thenReturn(Optional.of(new ShaHit(5L, "collect/old.xlsx")));

        SourceResult r = service.collect(source, new CollectPlan(1, START, null, false, false), 1L);

        ArgumentCaptor<NewFile> saved = ArgumentCaptor.forClass(NewFile.class);
        verify(files).insert(saved.capture());
        assertThat(saved.getValue().status()).isEqualTo("DUPLICATE");
        assertThat(saved.getValue().dupOfId()).isEqualTo(5L);
        assertThat(saved.getValue().s3Key()).isEqualTo("collect/old.xlsx");
        verify(s3, never()).putIfAbsent(any(), any(), any());
        assertThat(r.filesNew()).isZero();
    }

    @Test
    void 달을_지정하면_그_달만_받는다() throws Exception {
        when(adapter.attachments(any(), eq(jul))).thenReturn(List.of());

        service.collect(source, new CollectPlan(5, START, YearMonth.of(2026, 8), false, true), 1L);

        verify(adapter).attachments(any(), eq(aug));
        verify(adapter, never()).attachments(any(), eq(jul));
        // 1페이지에서 찾았으니 다음 페이지는 보지 않는다
        verify(adapter, never()).listPosts(any(), eq(2));
    }

    @Test
    void 목록을_못읽으면_기관_실패() throws Exception {
        when(adapter.listPosts(any(), eq(1))).thenThrow(new java.io.IOException("HTTP 503"));

        SourceResult r = service.collect(source, new CollectPlan(1, START, null, false, false), 1L);

        assertThat(r.failed()).isTrue();
        assertThat(r.error()).contains("HTTP 503");
    }
}
