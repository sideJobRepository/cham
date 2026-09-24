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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 전자정부 표준 게시판 (서구청·중구청·유성구청). 보드 ID 만 다르고 나머지는 같다.
 *
 * 목록: 제목이 fn_search_detail('B0000…') 로 POST 폼을 쏘지만 GET view.do?nttId= 로도 열린다.
 * 첨부: javascript:fn_egov_downFile('FILE_…','0') → /cmm/fms/FileDown.do?atchFileId=…&fileSn=0
 */
@Component
public class EgovBbsAdapter extends AbstractBoardAdapter {

    private static final Pattern DETAIL = Pattern.compile("fn_search_detail\\('([^']+)'\\)");
    private static final Pattern NTT_ID = Pattern.compile("nttId=([A-Za-z0-9]+)");
    private static final Pattern DOWN = Pattern.compile("fn_egov_downFile\\('([^']+)'\\s*,\\s*'(\\d+)'\\)");

    public EgovBbsAdapter(PoliteHttpClient http) {
        super(http);
    }

    @Override
    public EngineType engine() {
        return EngineType.EGOV_BBS;
    }

    @Override
    public List<PostRef> parseList(CollectSource source, Document doc) {
        List<PostRef> posts = new ArrayList<>();
        for (Element el : doc.select("[onclick*=fn_search_detail], a[href*=nttId=]")) {
            String key = group(DETAIL, el.attr("onclick"));
            if (key == null) key = group(NTT_ID, el.attr("href"));
            if (key == null) continue;
            String title = clean(el.select(".bbs-subject-txt").text());
            if (title == null) title = clean(el.text());
            posts.add(new PostRef(key, title, null, detailUrl(source, key, el)));
        }
        return distinctBy(posts, PostRef::postKey);
    }

    @Override
    public List<AttachmentRef> parseAttachments(CollectSource source, PostRef post, Document doc) {
        String origin = origin(doc);
        List<AttachmentRef> result = new ArrayList<>();
        for (Element a : doc.select("a[href*=fn_egov_downFile]")) {
            Matcher m = DOWN.matcher(a.attr("href"));
            if (!m.find()) continue;
            Element copy = a.clone();
            copy.select("i, .sr-only").remove();
            String name = stripSize(copy.text());
            String url = origin + "/cmm/fms/FileDown.do?atchFileId=" + m.group(1) + "&fileSn=" + m.group(2);
            result.add(new AttachmentRef(m.group(1) + ":" + m.group(2), url, name, post.detailUrl()));
        }
        return distinctBy(result, AttachmentRef::attachKey);
    }
}
