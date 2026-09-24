package com.cham.collector.adapter;

import com.cham.collector.domain.CollectSource;
import com.cham.collector.domain.Refs.AttachmentRef;
import com.cham.collector.domain.Refs.Downloaded;
import com.cham.collector.domain.Refs.PostRef;
import com.cham.collector.http.PoliteHttpClient;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.net.URI;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class AbstractBoardAdapter implements BoardAdapter {

    protected final PoliteHttpClient http;

    protected AbstractBoardAdapter(PoliteHttpClient http) {
        this.http = http;
    }

    @Override
    public List<PostRef> listPosts(CollectSource source, int page) throws IOException {
        return parseList(source, http.getDocument(source.pageUrl(page)));
    }

    @Override
    public List<AttachmentRef> attachments(CollectSource source, PostRef post) throws IOException {
        return parseAttachments(source, post, http.getDocument(post.detailUrl()));
    }

    @Override
    public Downloaded download(AttachmentRef attachment) throws IOException {
        return http.download(attachment.url(), attachment.referer());
    }

    // ── 도우미 ──

    /** 공백 정리한 글자. nbsp 도 공백으로 */
    protected static String clean(String s) {
        if (s == null) return null;
        String v = s.replace(' ', ' ').replaceAll("\\s+", " ").trim();
        return v.isEmpty() ? null : v;
    }

    protected static String group(Pattern pattern, String text) {
        if (text == null) return null;
        Matcher m = pattern.matcher(text);
        return m.find() ? m.group(1) : null;
    }

    /** 공지와 일반글에 같은 글이 두 번 나오는 게시판이 있다. 키가 같으면 앞의 것만 남긴다 */
    protected static <T> List<T> distinctBy(List<T> items, Function<T, String> key) {
        Set<String> seen = new HashSet<>();
        List<T> result = new ArrayList<>();
        for (T item : items) {
            if (seen.add(key.apply(item))) result.add(item);
        }
        return result;
    }

    /** 문서 주소의 scheme://host */
    protected static String origin(Document doc) {
        URI uri = URI.create(doc.location());
        return uri.getScheme() + "://" + uri.getHost() + (uri.getPort() > 0 ? ":" + uri.getPort() : "");
    }

    protected static String detailUrl(CollectSource source, String key, Element link) {
        String fromTemplate = source.detailUrlOf(key);
        return fromTemplate != null ? fromTemplate : link.absUrl("href");
    }

    /** 첨부 이름 뒤에 붙는 크기 표시를 뗀다: '파일.xlsx [23.8 KB]', '파일.pdf(119.3KB)' */
    protected static String stripSize(String name) {
        if (name == null) return null;
        return clean(name.replaceAll("\\s*[\\[(]\\s*[\\d.,]+\\s*[KMG]?B?\\s*[\\])]\\s*$", ""));
    }
}
