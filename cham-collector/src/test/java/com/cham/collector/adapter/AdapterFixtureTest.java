package com.cham.collector.adapter;

import com.cham.collector.domain.CollectSource;
import com.cham.collector.domain.EngineType;
import com.cham.collector.domain.Refs.AttachmentRef;
import com.cham.collector.domain.Refs.PostRef;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 2026-09-24 에 실제 사이트에서 받은 HTML(링크 둘레만 잘라 둠)로 셀렉터를 확인한다.
 * 기관이 게시판을 바꾸면 여기가 아니라 운영 수집 이력에 '게시물을 찾지 못했습니다' 로 먼저 드러난다.
 * 그때 fixture 를 새로 받아 여기를 고친다.
 */
class AdapterFixtureTest {

    @Test
    void 동구의회() throws IOException {
        CollectSource s = source("DONGGU_COUNCIL", EngineType.COUNCIL_B,
                "https://council.donggu.go.kr/kr/activity/bbs?bbs_id=cost",
                "https://council.donggu.go.kr/kr/activity/bbs?bbs_id=cost&reform=view&uid={postKey}", "cost");
        CouncilBAdapter adapter = new CouncilBAdapter(null);

        List<PostRef> posts = adapter.parseList(s, doc("DONGGU_COUNCIL/list.html", s.listUrl()));
        assertThat(posts).isNotEmpty();
        assertThat(posts.get(0).postKey()).isEqualTo("9AEAF6FFC0EC23F5C3BD0B15CB513731");
        assertThat(posts.get(0).title()).isEqualTo("2026년 8월 업무추진비 집행내역");
        assertThat(posts.get(0).detailUrl()).endsWith("reform=view&uid=9AEAF6FFC0EC23F5C3BD0B15CB513731");

        List<AttachmentRef> files = adapter.parseAttachments(s, posts.get(0), doc("DONGGU_COUNCIL/detail.html", posts.get(0).detailUrl()));
        assertThat(files).hasSize(1);
        assertThat(files.get(0).displayName()).isEqualTo("의회운영업무추진비(2026__8_)_집행내역.xlsx");
        assertThat(files.get(0).url()).isEqualTo("https://council.donggu.go.kr/kr/activity/bbs_process?reform=download&bbs_id=cost&uid=9954");
        assertThat(files.get(0).referer()).isEqualTo(posts.get(0).detailUrl());
    }

    @Test
    void 중구의회() throws IOException {
        CollectSource s = source("JUNGGU_COUNCIL", EngineType.COUNCIL_B,
                "https://council.djjunggu.go.kr/kr/costBBS.do",
                "https://council.djjunggu.go.kr/kr/costBBSview.do?uid={postKey}", null);
        CouncilBAdapter adapter = new CouncilBAdapter(null);

        List<PostRef> posts = adapter.parseList(s, doc("JUNGGU_COUNCIL/list.html", s.listUrl()));
        assertThat(posts.get(0).postKey()).isEqualTo("AD6DBEEBE85D45868DC8D1C3B5B202DE");
        assertThat(posts.get(0).title()).contains("2026.8월");
        // 목록의 첨부 내려받기 링크(download.do?uid=…)를 게시물로 잡지 않는다
        assertThat(posts).extracting(PostRef::postKey).doesNotContain("E43868E9F0B53122C6039424D43B6A9F");

        List<AttachmentRef> files = adapter.parseAttachments(s, posts.get(0), doc("JUNGGU_COUNCIL/detail.html", posts.get(0).detailUrl()));
        assertThat(files).extracting(AttachmentRef::displayName).containsExactly("업무추진비 공개 2608.xlsx");
        assertThat(files.get(0).attachKey()).isEqualTo("E43868E9F0B53122C6039424D43B6A9F");
    }

    @Test
    void 대덕구의회_첨부없는_달() throws IOException {
        CollectSource s = source("DAEDEOK_COUNCIL", EngineType.COUNCIL_B,
                "https://council.daedeok.go.kr/kr/costBBS.do",
                "https://council.daedeok.go.kr/kr/costBBSview.do?uid={postKey}", null);
        CouncilBAdapter adapter = new CouncilBAdapter(null);

        List<PostRef> posts = adapter.parseList(s, doc("DAEDEOK_COUNCIL/list.html", s.listUrl()));
        assertThat(posts.get(0).title()).isEqualTo("2026년 8월 업무추진비 공개");

        // 8월분은 '사용내역 없습니다' 로 첨부가 없다
        assertThat(adapter.parseAttachments(s, posts.get(0), doc("DAEDEOK_COUNCIL/detail.html", posts.get(0).detailUrl())))
                .isEmpty();
    }

    @Test
    void 중구청_전자정부() throws IOException {
        CollectSource s = source("JUNGGU_MAYOR", EngineType.EGOV_BBS,
                "https://www.djjunggu.go.kr/bbs/BBSMSTR_000000000103/list.do",
                "https://www.djjunggu.go.kr/bbs/BBSMSTR_000000000103/view.do?nttId={postKey}", "BBSMSTR_000000000103");
        EgovBbsAdapter adapter = new EgovBbsAdapter(null);

        List<PostRef> posts = adapter.parseList(s, doc("JUNGGU_MAYOR/list.html", s.listUrl()));
        assertThat(posts.get(0).postKey()).isEqualTo("B000000228715Kx2aB4");
        assertThat(posts.get(0).title()).isEqualTo("구청장 업무추진비 사용내역(2026년 8월)");

        List<AttachmentRef> files = adapter.parseAttachments(s, posts.get(0), doc("JUNGGU_MAYOR/detail.html", posts.get(0).detailUrl()));
        assertThat(files).hasSize(1);
        assertThat(files.get(0).displayName()).isEqualTo("구청장 업무추진비 사용내역(2026년 8월).xlsx");
        assertThat(files.get(0).url()).isEqualTo("https://www.djjunggu.go.kr/cmm/fms/FileDown.do?atchFileId=FILE_000000061910Qm2&fileSn=0");
        assertThat(files.get(0).attachKey()).isEqualTo("FILE_000000061910Qm2:0");
    }

    @Test
    void 서구청_PDF와_xlsx_둘다() throws IOException {
        CollectSource s = source("SEOGU_MAYOR", EngineType.EGOV_BBS,
                "https://www.seogu.go.kr/bbs/BBSMSTR_000000000571/list.do",
                "https://www.seogu.go.kr/bbs/BBSMSTR_000000000571/view.do?nttId={postKey}", "BBSMSTR_000000000571");
        EgovBbsAdapter adapter = new EgovBbsAdapter(null);

        List<PostRef> posts = adapter.parseList(s, doc("SEOGU_MAYOR/list.html", s.listUrl()));
        assertThat(posts.get(0).postKey()).isEqualTo("B000000219284Rx0uA2");
        assertThat(posts.get(0).title()).isEqualTo("2026년 8월중 구청장 업무추진비 집행내역");

        List<AttachmentRef> files = adapter.parseAttachments(s, posts.get(0), doc("SEOGU_MAYOR/detail.html", posts.get(0).detailUrl()));
        assertThat(files).extracting(AttachmentRef::displayName).containsExactly(
                "2026년 8월중 구청장 업무추진비 집행내역.pdf",
                "2026년 8월중 구청장 업무추진비 집행내역.xlsx");
        assertThat(files.get(1).attachKey()).isEqualTo("FILE_000000045088Tb3:1");
    }

    @Test
    void 유성구의회_그누보드() throws IOException {
        CollectSource s = source("YUSEONG_COUNCIL", EngineType.GNUBOARD,
                "https://yuseonggucouncil.go.kr/bbs/board.php?bo_table=0603",
                "https://yuseonggucouncil.go.kr/bbs/board.php?bo_table=0603&wr_id={postKey}", "0603");
        GnuboardAdapter adapter = new GnuboardAdapter(null);

        List<PostRef> posts = adapter.parseList(s, doc("YUSEONG_COUNCIL/list.html", s.listUrl()));
        assertThat(posts.get(0).postKey()).isEqualTo("91");
        assertThat(posts.get(0).title()).isEqualTo("2026년 8월 의회운영업무추진비 사용내역 공개");

        List<AttachmentRef> files = adapter.parseAttachments(s, posts.get(0), doc("YUSEONG_COUNCIL/detail.html", posts.get(0).detailUrl()));
        assertThat(files).extracting(AttachmentRef::displayName).containsExactly(
                "2026년 8월 의회운영업무추진비 집행내역.xlsx",
                "2026년 8월 의회운영업무추진비 집행내역의장.xlsx");
        assertThat(files).extracting(AttachmentRef::attachKey).containsExactly("no=0", "no=1");
    }

    @Test
    void 동구청() throws IOException {
        CollectSource s = source("DONGGU_MAYOR", EngineType.CUSTOM_DONGGU,
                "https://www.donggu.go.kr/dg/kor/article/secretBusiness",
                "https://www.donggu.go.kr/dg/kor/article/secretBusiness/{postKey}", null);
        DongguAdapter adapter = new DongguAdapter(null);

        List<PostRef> posts = adapter.parseList(s, doc("DONGGU_MAYOR/list.html", s.listUrl()));
        assertThat(posts.get(0).postKey()).isEqualTo("143489");
        assertThat(posts.get(0).title()).isEqualTo("2026년 8월 구청장 업무추진비 집행내역 공개");
        assertThat(posts.get(0).detailUrl()).isEqualTo("https://www.donggu.go.kr/dg/kor/article/secretBusiness/143489");

        List<AttachmentRef> files = adapter.parseAttachments(s, posts.get(0), doc("DONGGU_MAYOR/detail.html", posts.get(0).detailUrl()));
        assertThat(files).hasSize(1);
        assertThat(files.get(0).displayName()).isEqualTo("2026년 8월 업무추진비 세부집행내역.xlsx");
        assertThat(files.get(0).url()).startsWith("https://www.donggu.go.kr/dg/attach/");
    }

    @Test
    void 대덕구청_지금은_PDF() throws IOException {
        CollectSource s = source("DAEDEOK_MAYOR", EngineType.CUSTOM_DAEDEOK,
                "https://www.daedeok.go.kr/dpt/dpt02/DPT02010401_cmmBoardList.do",
                "https://www.daedeok.go.kr/dpt/dpt02/DPT02010401_cmmBoardView.do?boardId=DPT_000022&ntatcSeq={postKey}", "DPT_000022");
        DaedeokAdapter adapter = new DaedeokAdapter(null);

        List<PostRef> posts = adapter.parseList(s, doc("DAEDEOK_MAYOR/list.html", s.listUrl()));
        assertThat(posts.get(0).postKey()).isEqualTo("1107766227");
        assertThat(posts.get(0).title()).isEqualTo("2026년 8월 업무추진비 집행내역");

        List<AttachmentRef> files = adapter.parseAttachments(s, posts.get(0), doc("DAEDEOK_MAYOR/detail.html", posts.get(0).detailUrl()));
        // 같은 링크가 두 번 나오지만 하나로 친다
        assertThat(files).hasSize(1);
        assertThat(files.get(0).displayName()).isEqualTo("(26.8월분) 업무추진비 집행내역(홈페이지)_구청장.pdf");
    }

    @Test
    void 대전시_시장부시장_PDF_여러개() throws IOException {
        CollectSource s = source("DAEJEON_MAYOR", EngineType.CUSTOM_DAEJEON,
                "https://www.daejeon.go.kr/drh/open/drhDataOpen/drhDataOpenBoardView.do?boardSeq=1186&menuSeq=4804",
                "https://www.daejeon.go.kr/drh/open/drhDataOpen/drhDataOpenBoardArticleView.do?menuSeq=4804&boardSeq=1186&articleSeq={postKey}&subPageIndex=1",
                "1186");
        DaejeonAdapter adapter = new DaejeonAdapter(null);

        List<PostRef> posts = adapter.parseList(s, doc("DAEJEON_MAYOR/list.html", s.listUrl()));
        assertThat(posts.get(0).postKey()).isEqualTo("14831");
        assertThat(posts.get(0).title()).isEqualTo("2026년 8월중 업무추진비 사용내역 공개");
        assertThat(posts.get(1).title()).isEqualTo("2026년 7월중 업무추진비 사용내역 공개");

        List<AttachmentRef> files = adapter.parseAttachments(s, posts.get(0), doc("DAEJEON_MAYOR/detail.html", posts.get(0).detailUrl()));
        // 같은 파일 링크가 두 번(이름·버튼) 나오지만 하나로 친다
        assertThat(files).hasSizeGreaterThanOrEqualTo(3);
        assertThat(files.get(0).displayName()).isEqualTo("0. 26. 8월 사용내역공개.pdf");
        assertThat(files.get(0).url()).isEqualTo("https://www.daejeon.go.kr/FileUpload/DRH/202609/20260911105727250.pdf");
        assertThat(files.get(0).attachKey()).isEqualTo("FileUpload/DRH/202609/20260911105727250.pdf");
    }

    @Test
    void 대전시의회_위원회별_게시물() throws IOException {
        CollectSource s = source("DAEJEON_COUNCIL", EngineType.COUNCIL_A,
                "https://council.daejeon.go.kr/svc/inf/OperatingExpenseList.do",
                "https://council.daejeon.go.kr/svc/inf/OperatingExpenseView.do?bbsSn={postKey}", null);
        CouncilAAdapter adapter = new CouncilAAdapter(null);

        List<PostRef> posts = adapter.parseList(s, doc("DAEJEON_COUNCIL/list.html", s.listUrl()));
        assertThat(posts.get(0).postKey()).isEqualTo("63268");
        assertThat(posts.get(0).title()).isEqualTo("2026년 8월 업무추진비 집행내역(복지환경위원회)");
        assertThat(posts.get(0).detailUrl()).isEqualTo("https://council.daejeon.go.kr/svc/inf/OperatingExpenseView.do?bbsSn=63268");

        List<AttachmentRef> files = adapter.parseAttachments(s, posts.get(0), doc("DAEJEON_COUNCIL/detail.html", posts.get(0).detailUrl()));
        assertThat(files).extracting(AttachmentRef::displayName).containsExactly(
                "8월 의정운영공통경비 집행내역.pdf", "8월 의회운영업무추진비 집행내역.pdf", "8월 시책업무추진비 집행내역.pdf");
        assertThat(files.get(0).url()).isEqualTo("https://council.daejeon.go.kr/bbs/FileDownLoadProc.do?flSn=136517");
    }

    @Test
    void 서구의회_의정활동정보공개_게시판() throws IOException {
        CollectSource s = new CollectSource(1L, "SEOGU_COUNCIL", "서구의회", EngineType.COUNCIL_CMN,
                "https://www.seogucouncil.daejeon.kr/svc/info/CouncilManagerList.do",
                "https://www.seogucouncil.daejeon.kr/svc/info/CouncilManagerView.do?schBbsSn={postKey}&schKyCd=counciladmin",
                null, "schPageNo", "schKyCd=counciladmin", Set.of("xlsx", "xls"), null, null, "업무추진비", true);
        CouncilCmnAdapter adapter = new CouncilCmnAdapter(null);

        List<PostRef> posts = adapter.parseList(s, doc("SEOGU_COUNCIL/list.html", s.listUrl()));
        assertThat(posts.get(0).postKey()).isEqualTo("14724");
        assertThat(posts.get(0).title()).isEqualTo("2026년 8월 부의장 업무추진비 집행내역");
        assertThat(posts.get(0).detailUrl()).endsWith("CouncilManagerView.do?schBbsSn=14724&schKyCd=counciladmin");
        assertThat(s.wantsPost(posts.get(0).title())).isTrue();
        assertThat(s.wantsPost("2026년 의원별 겸직 현황")).isFalse();
        assertThat(s.pageUrl(2)).isEqualTo(
                "https://www.seogucouncil.daejeon.kr/svc/info/CouncilManagerList.do?schKyCd=counciladmin&schPageNo=2");

        // 상세 아래에 붙은 목록의 링크는 첨부로 잡지 않는다
        List<AttachmentRef> files = adapter.parseAttachments(s, posts.get(0), doc("SEOGU_COUNCIL/detail.html", posts.get(0).detailUrl()));
        assertThat(files).hasSize(1);
        assertThat(files.get(0).displayName()).isEqualTo("2026. 8월 업무추진비(부의장).xlsx");
        assertThat(files.get(0).url()).isEqualTo("https://www.seogucouncil.daejeon.kr/bbs/FileDownLoadProc.do?schBbsSn=8416&schKyCd=counciladmin");
    }

    private static CollectSource source(String code, EngineType engine, String listUrl, String detailUrl, String boardId) {
        return new CollectSource(1L, code, code, engine, listUrl, detailUrl, boardId, "page", null, Set.of("xlsx", "xls", "pdf"), null, null, null, true);
    }

    private static Document doc(String path, String baseUri) throws IOException {
        try (InputStream in = AdapterFixtureTest.class.getResourceAsStream("/fixtures/" + path)) {
            return Jsoup.parse(in, "UTF-8", baseUri);
        }
    }
}
