package com.cham.collector.http;

import com.cham.collector.config.CollectorProperties;
import com.cham.collector.domain.Refs.Downloaded;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.CookieManager;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 기관 사이트로 나가는 요청은 전부 여기를 지난다.
 *
 * 지자체 게시판은 약하다. 한 번에 하나씩, 요청 사이에 간격을 두고, 누가 보내는지 User-Agent 에 밝힌다.
 * 실패하면 조금 쉬었다가 두 번까지 다시 시도한다.
 */
@Slf4j
@Component
public class PoliteHttpClient {

    private static final Pattern CHARSET = Pattern.compile("charset=([\\w\\-]+)", Pattern.CASE_INSENSITIVE);

    private final CollectorProperties props;
    private final HttpClient client;
    private long lastRequestAt = 0;

    public PoliteHttpClient(CollectorProperties props) {
        this.props = props;
        this.client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(props.connectTimeoutMs()))
                .followRedirects(HttpClient.Redirect.NORMAL)
                // 전자정부 게시판은 목록에서 세션 쿠키를 주고 상세에서 본다
                .cookieHandler(new CookieManager())
                .sslContext(lenientSsl())
                .build();
    }

    /** HTML 을 받아 파싱한다. 문자셋은 응답 헤더 → 문서 meta 순으로 정한다 */
    public Document getDocument(String url) throws IOException {
        HttpResponse<byte[]> res = send(url, null);
        String contentType = res.headers().firstValue("Content-Type").orElse("");
        String charset = charsetOf(contentType).orElse(null);
        try (InputStream in = new ByteArrayInputStream(res.body())) {
            return Jsoup.parse(in, charset, url);
        }
    }

    /** 첨부를 받는다. max-file-bytes 를 넘으면 실패로 본다 */
    public Downloaded download(String url, String referer) throws IOException {
        HttpResponse<byte[]> res = send(url, referer);
        byte[] body = res.body();
        String contentType = res.headers().firstValue("Content-Type").orElse(null);
        if (contentType != null && contentType.toLowerCase().startsWith("text/html")) {
            // 파일 대신 오류 페이지나 로그인 페이지가 온 경우
            throw new IOException("파일 대신 HTML 이 왔습니다: " + url);
        }
        String disposition = res.headers().firstValue("Content-Disposition").orElse(null);
        return new Downloaded(body, ContentDispositionDecoder.filename(disposition).orElse(null), contentType);
    }

    private synchronized HttpResponse<byte[]> send(String url, String referer) throws IOException {
        IOException last = null;
        for (int attempt = 0; attempt <= props.retries(); attempt++) {
            if (attempt > 0) sleep(3000L * attempt);
            throttle();
            try {
                HttpRequest.Builder req = HttpRequest.newBuilder(URI.create(url))
                        .timeout(Duration.ofMillis(props.readTimeoutMs()))
                        .header("User-Agent", props.userAgent())
                        .header("Accept-Language", "ko-KR,ko;q=0.9")
                        .GET();
                if (referer != null) req.header("Referer", referer);

                HttpResponse<InputStream> res = client.send(req.build(), HttpResponse.BodyHandlers.ofInputStream());
                byte[] body = readLimited(res.body(), props.maxFileBytes(), url);
                int status = res.statusCode();
                if (status >= 500) {
                    last = new IOException("HTTP " + status + " " + url);
                    continue;
                }
                if (status >= 400) {
                    throw new IOException("HTTP " + status + " " + url);
                }
                return new BytesResponse(res, body);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IOException("중단됨: " + url, e);
            } catch (IOException e) {
                if (e.getMessage() != null && e.getMessage().startsWith("HTTP 4")) throw e;
                last = e;
                log.warn("요청 실패 ({}회째) {}: {}", attempt + 1, url, e.getMessage());
            }
        }
        throw last;
    }

    private void throttle() {
        long wait = lastRequestAt + props.requestDelayMs() - System.currentTimeMillis();
        if (wait > 0) sleep(wait);
        lastRequestAt = System.currentTimeMillis();
    }

    private static byte[] readLimited(InputStream in, long max, String url) throws IOException {
        try (in; ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            byte[] buf = new byte[8192];
            long total = 0;
            int n;
            while ((n = in.read(buf)) > 0) {
                total += n;
                if (total > max) throw new IOException("파일이 너무 큽니다(" + max + "B 초과): " + url);
                out.write(buf, 0, n);
            }
            return out.toByteArray();
        }
    }

    static Optional<String> charsetOf(String contentType) {
        Matcher m = CHARSET.matcher(contentType == null ? "" : contentType);
        return m.find() ? Optional.of(m.group(1)) : Optional.empty();
    }

    private static void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * 일부 지자체 서버는 중간 인증서를 빼먹고 보내 JDK 가 검증에 실패한다(브라우저는 알아서 채운다).
     * 받는 것은 공개 게시판의 공개 파일이고 보내는 것은 없으므로 검증을 끈다.
     */
    private static SSLContext lenientSsl() {
        try {
            TrustManager[] trustAll = {new X509TrustManager() {
                public void checkClientTrusted(X509Certificate[] c, String a) {
                }

                public void checkServerTrusted(X509Certificate[] c, String a) {
                }

                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }
            }};
            SSLContext ctx = SSLContext.getInstance("TLS");
            ctx.init(null, trustAll, new java.security.SecureRandom());
            return ctx;
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    /** 스트림으로 받은 응답을 바이트 응답처럼 쓰려고 감싼다 */
    private record BytesResponse(HttpResponse<InputStream> origin, byte[] body) implements HttpResponse<byte[]> {
        public int statusCode() { return origin.statusCode(); }
        public HttpRequest request() { return origin.request(); }
        public Optional<HttpResponse<byte[]>> previousResponse() { return Optional.empty(); }
        public java.net.http.HttpHeaders headers() { return origin.headers(); }
        public Optional<javax.net.ssl.SSLSession> sslSession() { return origin.sslSession(); }
        public URI uri() { return origin.uri(); }
        public HttpClient.Version version() { return origin.version(); }
    }
}
