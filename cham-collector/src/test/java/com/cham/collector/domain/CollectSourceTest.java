package com.cham.collector.domain;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class CollectSourceTest {

    // 대전시장: 한 달치 PDF 중 시장·부시장만 받는다 (2026-09-24 실제 파일명)
    private final CollectSource daejeon = new CollectSource(1L, "DAEJEON_MAYOR", "대전광역시장·부시장",
            EngineType.CUSTOM_DAEJEON, "https://x", null, null, "subPageIndex", null, Set.of("pdf"), "시장", "정무", null, true);

    @Test
    void 대전시는_시장_부시장_파일만() {
        assertThat(daejeon.wants("1.시장(‘26.8월).pdf", "pdf")).isTrue();
        assertThat(daejeon.wants("2. 행정부시장(‘26.7월).pdf", "pdf")).isTrue();
        assertThat(daejeon.wants("0. 26. 8월 사용내역공개.pdf", "pdf")).isFalse();
        assertThat(daejeon.wants("2. 정무과학(‘26.8월).pdf", "pdf")).isFalse();
        assertThat(daejeon.wants("3. 정무경제과학(‘26.7월)(수정).pdf", "pdf")).isFalse();
    }

    @Test
    void 규칙이_없으면_확장자만_본다() {
        CollectSource plain = new CollectSource(2L, "X", "X", EngineType.COUNCIL_B, "https://x", null, null,
                "page", null, Set.of("xlsx"), null, null, null, true);
        assertThat(plain.wants("아무거나.xlsx", "xlsx")).isTrue();
        assertThat(plain.wants("아무거나.hwp", "hwp")).isFalse();
    }
}
