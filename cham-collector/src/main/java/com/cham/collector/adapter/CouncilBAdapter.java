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
 * 의회 게시판 계열 (중구의회·대덕구의회: kr/costBBS.do → kr/costBBSview.do?uid=,
 * 동구의회: kr/activity/bbs?bbs_id=cost&reform=view&uid=). uid 는 32자리 16진수다.
 *
 * 첨부: 중구·대덕 /kr/bbs/download.do?bbs_id=cost&uid=…, 동구 /kr/activity/bbs_process?reform=download&uid=…
 * 목록에도 첨부 링크가 같이 보이지만 게시물과 짝을 맞추기 어려워 상세에서 받는다.
 */
@Component
public class CouncilBAdapter extends AbstractBoardAdapter {

    private static final Pattern UID = Pattern.compile("[?&]uid=([A-Za-z0-9]+)");

    public CouncilBAdapter(PoliteHttpClient http) {
        super(http);
    }

    @Override
    public EngineType engine() {
        return EngineType.COUNCIL_B;
    }

    @Override
    public List<PostRef> parseList(CollectSource source, Document doc) {
        List<PostRef> posts = new ArrayList<>();
        for (Element a : doc.select("a[href*=uid=]")) {
            String href = a.attr("href");
            if (!href.contains("costBBSview") && !href.contains("reform=view")) continue;
            String key = group(UID, href);
            if (key == null) continue;
            String title = clean(a.text());
            if (title == null) title = clean(a.attr("title").replaceAll("\\s*(상세보기|내용보기)$", ""));
            posts.add(new PostRef(key, title, null, detailUrl(source, key, a)));
        }
        return distinctBy(posts, PostRef::postKey);
    }

    @Override
    public List<AttachmentRef> parseAttachments(CollectSource source, PostRef post, Document doc) {
        List<AttachmentRef> result = new ArrayList<>();
        for (Element a : doc.select("a[href*=download]")) {
            String href = a.attr("href");
            if (!href.contains("bbs/download.do") && !href.contains("reform=download")) continue;
            String key = group(UID, href);
            if (key == null) continue;
            String name = clean(a.select("span.name").text());
            if (name == null) name = clean(a.attr("title").replaceAll("^'|'?\\s*파일 내려받기$|'$", ""));
            if (name == null) name = clean(a.text());
            result.add(new AttachmentRef(key, a.absUrl("href"), stripSize(name), post.detailUrl()));
        }
        return distinctBy(result, AttachmentRef::attachKey);
    }
}
