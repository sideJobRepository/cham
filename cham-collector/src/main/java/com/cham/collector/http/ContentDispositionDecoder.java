package com.cham.collector.http;

import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.nio.ByteBuffer;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Content-Disposition 의 한글 파일명 풀기.
 *
 * 기관마다 제각각이다. filename*=UTF-8''%EC%97… 처럼 제대로 주는 곳, filename="…" 에 UTF-8 바이트를
 * 그대로 넣는 곳, EUC-KR 바이트를 넣는 곳, URL 인코딩만 해서 넣는 곳이 섞여 있다.
 * JDK 는 헤더를 ISO-8859-1 로 읽으므로 그 바이트를 되살려 UTF-8 → EUC-KR 순으로 맞춰 본다.
 */
public final class ContentDispositionDecoder {

    private static final Pattern EXTENDED = Pattern.compile("filename\\*\\s*=\\s*([\\w\\-]+)'[^']*'([^;]+)", Pattern.CASE_INSENSITIVE);
    private static final Pattern PLAIN = Pattern.compile("filename\\s*=\\s*(\"([^\"]*)\"|([^;]+))", Pattern.CASE_INSENSITIVE);
    private static final Charset EUC_KR = Charset.forName("MS949");

    public static Optional<String> filename(String header) {
        if (header == null || header.isBlank()) return Optional.empty();

        Matcher ext = EXTENDED.matcher(header);
        if (ext.find()) {
            try {
                return clean(URLDecoder.decode(ext.group(2).trim(), Charset.forName(ext.group(1))));
            } catch (RuntimeException ignored) {
                // 아래 일반 filename 으로 넘어간다
            }
        }

        Matcher plain = PLAIN.matcher(header);
        if (!plain.find()) return Optional.empty();
        String raw = plain.group(2) != null ? plain.group(2) : plain.group(3).trim();

        // URL 인코딩만 된 경우
        if (raw.matches(".*%[0-9A-Fa-f]{2}.*")) {
            try {
                return clean(URLDecoder.decode(raw.replace("+", "%2B"), StandardCharsets.UTF_8));
            } catch (RuntimeException ignored) {
                // 그대로 둔다
            }
        }

        // ISO-8859-1 로 읽힌 바이트를 되살린다. 한 글자라도 255 를 넘으면 이미 제대로 풀린 문자열이다
        if (raw.chars().allMatch(c -> c < 256) && raw.chars().anyMatch(c -> c >= 128)) {
            byte[] bytes = raw.getBytes(StandardCharsets.ISO_8859_1);
            Optional<String> utf8 = strictDecode(bytes, StandardCharsets.UTF_8);
            if (utf8.isPresent()) return clean(utf8.get());
            Optional<String> euckr = strictDecode(bytes, EUC_KR);
            if (euckr.isPresent()) return clean(euckr.get());
        }
        return clean(raw);
    }

    private static Optional<String> strictDecode(byte[] bytes, Charset charset) {
        try {
            return Optional.of(charset.newDecoder()
                    .onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT)
                    .decode(ByteBuffer.wrap(bytes))
                    .toString());
        } catch (CharacterCodingException e) {
            return Optional.empty();
        }
    }

    private static Optional<String> clean(String name) {
        if (name == null) return Optional.empty();
        String n = name.trim();
        if (n.startsWith("\"") && n.endsWith("\"") && n.length() > 1) n = n.substring(1, n.length() - 1);
        // 경로가 붙어 오는 경우가 있다
        int slash = Math.max(n.lastIndexOf('/'), n.lastIndexOf('\\'));
        if (slash >= 0) n = n.substring(slash + 1);
        return n.isBlank() ? Optional.empty() : Optional.of(n);
    }

    private ContentDispositionDecoder() {
    }
}
