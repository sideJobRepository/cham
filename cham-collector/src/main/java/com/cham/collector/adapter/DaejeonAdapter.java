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
 * 대전광역시 사전정보공개. 원래 3단(분류 목록 → '시장, 부시장' 항목 → 월별 게시물)이지만
 * 목록 URL 을 '시장, 부시장' 항목(drhDataOpenBoardView.do?boardSeq=1186)으로 잡아 2단으로 쓴다.
 * 페이지는 subPageIndex. 게시물은 drhDataOpenBoardArticleView.do?…&articleSeq=N.
 *
 * 첨부는 href 가 javascript:fileDownLoad('FileUpload/DRH/202609/….pdf', '원본파일명') 이다.
 * 첫 인자가 서버 경로라 https://www.daejeon.go.kr/{경로} 로 바로 받아진다(2026-09-24 확인).
 * 한 달치에 PDF 가 여러 개(사용내역공개 / 시장 / 정무과학 …) 붙는다.
 */
@Component
public class DaejeonAdapter extends AbstractBoardAdapter {

    private static final Pattern ARTICLE = Pattern.compile("articleSeq=(\\d+)");
    private static final Pattern DOWN = Pattern.compile("fileDownLoad\\('([^']+)'\\s*,\\s*'([^']*)'\\)");

    public DaejeonAdapter(PoliteHttpClient http) {
        super(http);
    }

    @Override
    public EngineType engine() {
        return EngineType.CUSTOM_DAEJEON;
    }

    @Override
    public List<PostRef> parseList(CollectSource source, Document doc) {
        List<PostRef> posts = new ArrayList<>();
        for (Element a : doc.select("a[href*=drhDataOpenBoardArticleView]")) {
            String key = group(ARTICLE, a.attr("href"));
            if (key == null) continue;
            posts.add(new PostRef(key, clean(a.text()), null, detailUrl(source, key, a)));
        }
        return distinctBy(posts, PostRef::postKey);
    }

    @Override
    public List<AttachmentRef> parseAttachments(CollectSource source, PostRef post, Document doc) {
        String origin = origin(doc);
        List<AttachmentRef> result = new ArrayList<>();
        for (Element a : doc.select("a[href*=fileDownLoad]")) {
            Matcher m = DOWN.matcher(a.attr("href"));
            if (!m.find()) continue;
            String path = m.group(1).replaceAll("^/+", "");
            // 파일명의 ‘ ’ 같은 둥근 따옴표는 그대로 둔다(원본 이름)
            result.add(new AttachmentRef(path, origin + "/" + path, clean(m.group(2)), post.detailUrl()));
        }
        return distinctBy(result, AttachmentRef::attachKey);
    }
}
