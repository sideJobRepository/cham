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
 * 그누보드 (유성구의회). board.php?bo_table=0603&wr_id=N → 첨부 a.view_file_download (download.php?…&no=0)
 */
@Component
public class GnuboardAdapter extends AbstractBoardAdapter {

    private static final Pattern WR_ID = Pattern.compile("[?&]wr_id=(\\d+)");
    private static final Pattern NO = Pattern.compile("[?&]no=(\\d+)");

    public GnuboardAdapter(PoliteHttpClient http) {
        super(http);
    }

    @Override
    public EngineType engine() {
        return EngineType.GNUBOARD;
    }

    @Override
    public List<PostRef> parseList(CollectSource source, Document doc) {
        List<PostRef> posts = new ArrayList<>();
        // 제목 링크에만 붙는 클래스. 없으면(스킨이 바뀌면) wr_id 링크 전부를 본다
        List<Element> links = doc.select("a.list-subject-link[href*=wr_id=]");
        if (links.isEmpty()) links = doc.select("a[href*=wr_id=]");
        for (Element a : links) {
            String href = a.attr("href");
            if (source.boardId() != null && !href.contains("bo_table=" + source.boardId())) continue;
            String key = group(WR_ID, href);
            if (key == null) continue;
            posts.add(new PostRef(key, clean(a.text()), null, detailUrl(source, key, a)));
        }
        return distinctBy(posts, PostRef::postKey);
    }

    @Override
    public List<AttachmentRef> parseAttachments(CollectSource source, PostRef post, Document doc) {
        List<AttachmentRef> result = new ArrayList<>();
        for (Element a : doc.select("a.view_file_download, a[href*=download.php]")) {
            String no = group(NO, a.attr("href"));
            if (no == null) continue;
            Element nameEl = a.selectFirst(".view-attach-name");
            String name;
            if (nameEl != null) {
                Element copy = nameEl.clone();
                copy.select(".sound_only").remove();
                name = clean(copy.text());
            } else {
                name = clean(a.text());
            }
            result.add(new AttachmentRef("no=" + no, a.absUrl("href"), name, post.detailUrl()));
        }
        return distinctBy(result, AttachmentRef::attachKey);
    }
}
