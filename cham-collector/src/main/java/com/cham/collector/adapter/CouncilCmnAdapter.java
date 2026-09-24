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
 * 서구의회 '의정활동 정보공개 > 의회운영' 게시판 (svc/info/CouncilManagerList.do).
 * 서구의회는 '업무추진비' 메뉴(svc/cdr/OperatingExpenseList.do)를 비워 두고 여기에 올린다(2026-09-24 확인).
 * 의정비·겸직 현황 같은 글이 섞여 있어 기관 설정의 게시물 제목 규칙('업무추진비')으로 거른다.
 *
 * 목록 제목은 goBbsCmnViewPage('14724','counciladmin',…) JS 인데 GET 으로도 열린다.
 *   목록 CouncilManagerList.do?schPageNo=N&schKyCd=counciladmin
 *   상세 CouncilManagerView.do?schBbsSn={글번호}&schKyCd=counciladmin
 *   첨부 /bbs/FileDownLoadProc.do?schBbsSn=N&schKyCd=counciladmin (xlsx)
 */
@Component
public class CouncilCmnAdapter extends AbstractBoardAdapter {

    private static final Pattern VIEW = Pattern.compile("goBbsCmnViewPage\\('(\\d+)'");
    private static final Pattern FILE_SN = Pattern.compile("(?:schBbsSn|flSn)=(\\d+)");

    public CouncilCmnAdapter(PoliteHttpClient http) {
        super(http);
    }

    @Override
    public EngineType engine() {
        return EngineType.COUNCIL_CMN;
    }

    @Override
    public List<PostRef> parseList(CollectSource source, Document doc) {
        List<PostRef> posts = new ArrayList<>();
        for (Element a : doc.select("a[onclick*=goBbsCmnViewPage]")) {
            String key = group(VIEW, a.attr("onclick"));
            if (key == null) continue;
            posts.add(new PostRef(key, clean(a.text()), null, detailUrl(source, key, a)));
        }
        return distinctBy(posts, PostRef::postKey);
    }

    @Override
    public List<AttachmentRef> parseAttachments(CollectSource source, PostRef post, Document doc) {
        List<AttachmentRef> result = new ArrayList<>();
        // 상세 아래에 목록이 같이 붙어 나오므로 첨부 칸(td.addfile) 안만 본다
        for (Element a : doc.select(".addfile a[href*=FileDownLoadProc.do]")) {
            String key = group(FILE_SN, a.attr("href"));
            if (key == null) continue;
            result.add(new AttachmentRef(key, a.absUrl("href"), stripSize(clean(a.text())), post.detailUrl()));
        }
        return distinctBy(result, AttachmentRef::attachKey);
    }
}
