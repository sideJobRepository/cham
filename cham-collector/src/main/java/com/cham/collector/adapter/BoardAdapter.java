package com.cham.collector.adapter;

import com.cham.collector.domain.CollectSource;
import com.cham.collector.domain.EngineType;
import com.cham.collector.domain.Refs.AttachmentRef;
import com.cham.collector.domain.Refs.Downloaded;
import com.cham.collector.domain.Refs.PostRef;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.List;

/**
 * 게시판 엔진 하나. "목록 → 상세 → 첨부" 3단은 모든 기관이 같고 HTML 모양만 다르다.
 * parseList / parseAttachments 는 네트워크 없이 저장해 둔 HTML 로 테스트한다.
 */
public interface BoardAdapter {

    EngineType engine();

    List<PostRef> listPosts(CollectSource source, int page) throws IOException;

    List<AttachmentRef> attachments(CollectSource source, PostRef post) throws IOException;

    Downloaded download(AttachmentRef attachment) throws IOException;

    List<PostRef> parseList(CollectSource source, Document doc);

    List<AttachmentRef> parseAttachments(CollectSource source, PostRef post, Document doc);
}
