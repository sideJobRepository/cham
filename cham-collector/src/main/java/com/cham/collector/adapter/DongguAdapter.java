package com.cham.collector.adapter;

import com.cham.collector.domain.CollectSource;
import com.cham.collector.domain.EngineType;
import com.cham.collector.domain.Refs.AttachmentRef;
import com.cham.collector.domain.Refs.PostRef;
import com.cham.collector.http.PoliteHttpClient;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 동구청 자체 CMS. 목록 제목이 article.view('143489') 이고, 이게 /dg/kor/article/secretBusiness/143489 로 GET 한다
 * (Article.js 확인). 첨부는 a.icon_file[href=/dg/attach/{해시}/{해시}], 옆의 /dg/attach/preview/… 는 미리보기라 뺀다.
 */
@Component
public class DongguAdapter extends AbstractBoardAdapter {

    private static final Pattern VIEW = Pattern.compile("article\\.view\\('(\\d+)'\\)");

    public DongguAdapter(PoliteHttpClient http) {
        super(http);
    }

    @Override
    public EngineType engine() {
        return EngineType.CUSTOM_DONGGU;
    }

    @Override
    public List<PostRef> parseList(CollectSource source, Document doc) {
        List<PostRef> posts = new ArrayList<>();
        for (Element a : doc.select("a[onclick*=article.view]")) {
            String key = group(VIEW, a.attr("onclick"));
            if (key == null) continue;
            String detail = source.detailUrlOf(key);
            if (detail == null) detail = source.listUrl().replaceAll("/+$", "") + "/" + key;
            posts.add(new PostRef(key, clean(a.text()), null, detail));
        }
        return distinctBy(posts, PostRef::postKey);
    }

    @Override
    public List<AttachmentRef> parseAttachments(CollectSource source, PostRef post, Document doc) {
        List<AttachmentRef> result = new ArrayList<>();
        for (Element a : doc.select("a[href*=/attach/]")) {
            String href = a.attr("href");
            // 옆에 붙은 '미리보기'(/attach/preview/…)는 파일이 아니다
            if (href.contains("/attach/preview/")) continue;
            String key = href.replaceAll("^.*/attach/", "");
            result.add(new AttachmentRef(key, a.absUrl("href"), stripSize(clean(a.text())), post.detailUrl()));
        }
        return distinctBy(result, AttachmentRef::attachKey);
    }
}
