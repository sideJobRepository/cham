package com.cham.collector.http;

import org.junit.jupiter.api.Test;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class ContentDispositionDecoderTest {

    private static final String NAME = "업무추진비 공개 2608.xlsx";

    @Test
    void RFC5987_형식() {
        assertThat(ContentDispositionDecoder.filename(
                "attachment; filename*=UTF-8''%EC%97%85%EB%AC%B4%EC%B6%94%EC%A7%84%EB%B9%84%20%EA%B3%B5%EA%B0%9C%202608.xlsx"))
                .contains(NAME);
    }

    @Test
    void UTF8_바이트를_그대로_넣은_경우() {
        // JDK 는 헤더를 ISO-8859-1 로 읽는다
        String header = "attachment; filename=\"" + new String(NAME.getBytes(StandardCharsets.UTF_8), StandardCharsets.ISO_8859_1) + "\"";
        assertThat(ContentDispositionDecoder.filename(header)).contains(NAME);
    }

    @Test
    void EUC_KR_바이트를_넣은_경우() {
        String header = "attachment; filename=\"" + new String(NAME.getBytes(Charset.forName("MS949")), StandardCharsets.ISO_8859_1) + "\"";
        assertThat(ContentDispositionDecoder.filename(header)).contains(NAME);
    }

    @Test
    void URL_인코딩만_한_경우() {
        assertThat(ContentDispositionDecoder.filename(
                "attachment; filename=%EC%97%85%EB%AC%B4%EC%B6%94%EC%A7%84%EB%B9%84%20%EA%B3%B5%EA%B0%9C%202608.xlsx;"))
                .contains(NAME);
    }

    @Test
    void 없으면_비어있다() {
        assertThat(ContentDispositionDecoder.filename(null)).isEmpty();
        assertThat(ContentDispositionDecoder.filename("inline")).isEmpty();
    }
}
