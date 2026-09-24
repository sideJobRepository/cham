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
 * 대덕구청 자체 CMS. DPT02010401_cmmBoardList.do → DPT02010401_cmmBoardView.do?boardId=DPT_000022&ntatcSeq=N,
 * 첨부 /board/binary/DPT_000022/{번호}.{확장자}.
 * 2026년 8월분부터 PDF 만 올려서 지금은 꺼 두었다(xlsx 로 돌아오면 켠다).
 */
@Component
public class DaedeokAdapter extends AbstractBoardAdapter {

    private static final Pattern SEQ = Pattern.compile("ntatcSeq=(\\d+)");

    public DaedeokAdapter(PoliteHttpClient http) {
        super(http);
    }

    @Override
    public EngineType engine() {
        return EngineType.CUSTOM_DAEDEOK;
    }

    @Override
    public List<PostRef> parseList(CollectSource source, Document doc) {
        List<PostRef> posts = new ArrayList<>();
        for (Element a : doc.select("a[href*=ntatcSeq=]")) {
            String key = group(SEQ, a.attr("href"));
            if (key == null) continue;
            // 모바일용 '날짜 | 작성자 | 제목' 줄이 링크 안에 같이 들어 있다
            Element copy = a.clone();
            copy.select(".mobile_con").remove();
            posts.add(new PostRef(key, clean(copy.text()), null, detailUrl(source, key, a)));
        }
        return distinctBy(posts, PostRef::postKey);
    }

    @Override
    public List<AttachmentRef> parseAttachments(CollectSource source, PostRef post, Document doc) {
        List<AttachmentRef> result = new ArrayList<>();
        for (Element a : doc.select("a[href*=/board/binary/]")) {
            String href = a.attr("href");
            String key = href.replaceAll("^.*/board/binary/", "");
            result.add(new AttachmentRef(key, a.absUrl("href"), stripSize(clean(a.text())), post.detailUrl()));
        }
        return distinctBy(result, AttachmentRef::attachKey);
    }
}
