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
 * 의회A 계열 (대전시의회: svc/inf/OperatingExpenseList.do, 서구의회: svc/cdr/…).
 * 목록은 폼에 CSRFToken 이 있지만 GET ?pageNo=N 으로도 열린다(2026-09-24 확인).
 * 게시물 OperatingExpenseView.do?bbsSn=N, 첨부 /bbs/FileDownLoadProc.do?flSn=N.
 *
 * 대전시의회는 한 달에 위원회별로 게시물이 여러 건 올라오고, 게시물마다 PDF 가 여러 개 붙는다.
 */
@Component
public class CouncilAAdapter extends AbstractBoardAdapter {

    private static final Pattern BBS_SN = Pattern.compile("bbsSn=(\\d+)");
    private static final Pattern FL_SN = Pattern.compile("flSn=(\\d+)");

    public CouncilAAdapter(PoliteHttpClient http) {
        super(http);
    }

    @Override
    public EngineType engine() {
        return EngineType.COUNCIL_A;
    }

    @Override
    public List<PostRef> parseList(CollectSource source, Document doc) {
        List<PostRef> posts = new ArrayList<>();
        for (Element a : doc.select("a[href*=OperatingExpenseView.do]")) {
            String key = group(BBS_SN, a.attr("href"));
            if (key == null) continue;
            posts.add(new PostRef(key, clean(a.text()), null, detailUrl(source, key, a)));
        }
        return distinctBy(posts, PostRef::postKey);
    }

    @Override
    public List<AttachmentRef> parseAttachments(CollectSource source, PostRef post, Document doc) {
        List<AttachmentRef> result = new ArrayList<>();
        for (Element a : doc.select("a[href*=FileDownLoadProc.do]")) {
            String key = group(FL_SN, a.attr("href"));
            if (key == null) continue;
            // title="8월 … 집행내역.pdf, 첨부파일을 다운받습니다."
            String name = clean(a.attr("title").replaceAll("\\s*,\\s*첨부파일.*$", ""));
            if (name == null) name = clean(a.text());
            result.add(new AttachmentRef(key, a.absUrl("href"), stripSize(name), post.detailUrl()));
        }
        return distinctBy(result, AttachmentRef::attachKey);
    }
}
