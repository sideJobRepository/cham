-- 업무추진비 자동수집 테이블 (cham-collector 와 cham-api 가 같이 쓴다)
-- 한 번에 실행해도 에러가 나지 않도록 참조 순서대로 정렬했다.
--
-- 누가 무엇을 쓰는가
--   cham-collector : COLLECT_FILE 에 COLLECTED / DUPLICATE 행을 insert 만 한다.
--                    COLLECT_JOB 을 claim(REQUESTED→RUNNING) 하고 결과를 갱신한다.
--                    COLLECT_SOURCE 의 LAST_SUCCESS / LAST_ERROR 를 갱신한다.
--   cham-api       : COLLECT_FILE 의 상태 전이(IMPORTED/IGNORED/FAILED)와 DELKEY/IMPORT_* 를 쓴다.
--                    '지금 수집' 버튼으로 COLLECT_JOB 에 REQUESTED 행을 넣는다. 달을 고르면 TARGET_YEAR/MONTH 가 찬다.
--
-- 수집 규칙
--   - 대상은 2026년 7월분부터. 그 전 달 게시물은 받지 않는다 (collector.min-period)
--   - 매일 한 번 돈다. 기관이 언제 올릴지 모르기 때문이다
--   - 정기 실행은 기관마다 목록 1페이지만 본다. 이미 받은 게시물은 상세도 열지 않는다
--     (대전시의회는 한 달에 위원회별로 게시물이 여러 건이라 '그 달 있으면 건너뜀' 을 쓰지 않는다)
--   - 달을 지정한 작업은 그 달 게시물만 받는다. 목록을 몇 페이지 더 넘겨 찾는다


-- CHAM_MONIMAP_COLLECT_SOURCE Table Create SQL
CREATE TABLE CHAM.CHAM_MONIMAP_COLLECT_SOURCE
(
    `CHAM_MONIMAP_COLLECT_SOURCE_ID`            BIGINT           NOT NULL    AUTO_INCREMENT COMMENT '자치연대 예산감시 수집 출처 ID',
    `CHAM_MONIMAP_COLLECT_SOURCE_CODE`          VARCHAR(50)      NOT NULL    COMMENT '자치연대 예산감시 수집 출처 코드 (S3 경로용)',
    `CHAM_MONIMAP_COLLECT_SOURCE_NAME`          VARCHAR(100)     NOT NULL    COMMENT '자치연대 예산감시 수집 출처 기관명',
    `CHAM_MONIMAP_COLLECT_SOURCE_ENGINE`        VARCHAR(30)      NOT NULL    COMMENT '자치연대 예산감시 수집 출처 게시판 엔진 (EGOV_BBS/COUNCIL_B/COUNCIL_A/GNUBOARD/CUSTOM_DONGGU/CUSTOM_DAEDEOK/CUSTOM_DAEJEON)',
    `CHAM_MONIMAP_COLLECT_SOURCE_LIST_URL`      VARCHAR(1000)    NOT NULL    COMMENT '자치연대 예산감시 수집 출처 목록 URL',
    `CHAM_MONIMAP_COLLECT_SOURCE_DETAIL_URL`    VARCHAR(1000)    NULL        COMMENT '자치연대 예산감시 수집 출처 상세 URL 템플릿 ({postKey} 치환)',
    `CHAM_MONIMAP_COLLECT_SOURCE_BOARD_ID`      VARCHAR(100)     NULL        COMMENT '자치연대 예산감시 수집 출처 게시판 ID',
    `CHAM_MONIMAP_COLLECT_SOURCE_PAGE_PARAM`    VARCHAR(30)      NULL        COMMENT '자치연대 예산감시 수집 출처 페이지 파라미터명 (page/pageIndex/pageNo)',
    `CHAM_MONIMAP_COLLECT_SOURCE_EXTRA_PARAM`   VARCHAR(1000)    NULL        COMMENT '자치연대 예산감시 수집 출처 추가 파라미터 (쿼리스트링)',
    `CHAM_MONIMAP_COLLECT_SOURCE_POSITION_NAME` VARCHAR(100)     NOT NULL    COMMENT '자치연대 예산감시 수집 출처 기본 카드 승인자 직위명 (CARD_OWNER_POSITION_NAME)',
    `CHAM_MONIMAP_COLLECT_SOURCE_REGION`        VARCHAR(100)     NOT NULL    COMMENT '자치연대 예산감시 수집 출처 기본 지역',
    `CHAM_MONIMAP_COLLECT_SOURCE_DEFAULT_USER`  VARCHAR(100)     NULL        COMMENT '자치연대 예산감시 수집 출처 기본 사용자(직함) (엑셀 2열 사용자)',
    `CHAM_MONIMAP_COLLECT_SOURCE_DEFAULT_NAME`  VARCHAR(100)     NULL        COMMENT '자치연대 예산감시 수집 출처 기본 이름 (엑셀 3열 이름)',
    `CHAM_MONIMAP_COLLECT_SOURCE_FILE_FORMAT`   VARCHAR(20)      NOT NULL    COMMENT '자치연대 예산감시 수집 출처 원본 형식 (XLSX/PDF/XLSX_PDF/NONE)',
    `CHAM_MONIMAP_COLLECT_SOURCE_ALLOW_EXT`     VARCHAR(100)     NOT NULL    DEFAULT 'xlsx,xls' COMMENT '자치연대 예산감시 수집 출처 수집 허용 확장자',
    `CHAM_MONIMAP_COLLECT_SOURCE_ATTACH_INCLUDE` VARCHAR(500)   NULL        COMMENT '자치연대 예산감시 수집 출처 받을 첨부 파일명 정규식 (NULL=전부)',
    `CHAM_MONIMAP_COLLECT_SOURCE_ATTACH_EXCLUDE` VARCHAR(500)   NULL        COMMENT '자치연대 예산감시 수집 출처 뺄 첨부 파일명 정규식 (NULL=없음)',
    `CHAM_MONIMAP_COLLECT_SOURCE_ENABLED`       TINYINT(1)       NOT NULL    DEFAULT 0 COMMENT '자치연대 예산감시 수집 출처 사용 여부',
    `CHAM_MONIMAP_COLLECT_SOURCE_SORT`          INT              NOT NULL    DEFAULT 0 COMMENT '자치연대 예산감시 수집 출처 정렬 순서',
    `CHAM_MONIMAP_COLLECT_SOURCE_LAST_SUCCESS`  DATETIME         NULL        COMMENT '자치연대 예산감시 수집 출처 마지막 성공 일시',
    `CHAM_MONIMAP_COLLECT_SOURCE_LAST_ERROR`    VARCHAR(2000)    NULL        COMMENT '자치연대 예산감시 수집 출처 마지막 오류',
    `CHAM_MONIMAP_COLLECT_SOURCE_NOTE`          VARCHAR(2000)    NULL        COMMENT '자치연대 예산감시 수집 출처 비고',
    `REGIST_DATE`                               DATETIME         NULL        COMMENT '생성 일시',
    `MODIFY_DATE`                               DATETIME         NULL        COMMENT '수정 일시',
    PRIMARY KEY (CHAM_MONIMAP_COLLECT_SOURCE_ID),
    UNIQUE KEY UK_CHAM_MONIMAP_COLLECT_SOURCE_CODE (CHAM_MONIMAP_COLLECT_SOURCE_CODE)
);

ALTER TABLE CHAM.CHAM_MONIMAP_COLLECT_SOURCE COMMENT '자치연대_예산감시_수집_출처';


-- CHAM_MONIMAP_COLLECT_JOB Table Create SQL
CREATE TABLE CHAM.CHAM_MONIMAP_COLLECT_JOB
(
    `CHAM_MONIMAP_COLLECT_JOB_ID`            BIGINT        NOT NULL    AUTO_INCREMENT COMMENT '자치연대 예산감시 수집 작업 ID',
    `CHAM_MONIMAP_COLLECT_JOB_TRIGGER`       VARCHAR(20)   NOT NULL    COMMENT '자치연대 예산감시 수집 작업 실행 구분 (SCHEDULED/MANUAL)',
    `CHAM_MONIMAP_COLLECT_JOB_REQUESTED_BY`  BIGINT        NULL        COMMENT '자치연대 예산감시 수집 작업 요청 회원 ID',
    `CHAM_MONIMAP_COLLECT_SOURCE_ID`         BIGINT        NULL        COMMENT '자치연대 예산감시 수집 작업 대상 출처 ID (NULL=사용중 전체)',
    `CHAM_MONIMAP_COLLECT_JOB_PAGE_LIMIT`    INT           NULL        COMMENT '자치연대 예산감시 수집 작업 목록 페이지 수 (NULL=기본값, 백필용)',
    `CHAM_MONIMAP_COLLECT_JOB_TARGET_YEAR`   INT           NULL        COMMENT '자치연대 예산감시 수집 작업 대상 연도 (NULL=달 지정 없음)',
    `CHAM_MONIMAP_COLLECT_JOB_TARGET_MONTH`  INT           NULL        COMMENT '자치연대 예산감시 수집 작업 대상 월 (NULL=달 지정 없음)',
    `CHAM_MONIMAP_COLLECT_JOB_STATUS`        VARCHAR(20)   NOT NULL    COMMENT '자치연대 예산감시 수집 작업 상태 (REQUESTED/RUNNING/DONE/FAILED)',
    `CHAM_MONIMAP_COLLECT_JOB_STARTED_AT`    DATETIME      NULL        COMMENT '자치연대 예산감시 수집 작업 시작 일시',
    `CHAM_MONIMAP_COLLECT_JOB_FINISHED_AT`   DATETIME      NULL        COMMENT '자치연대 예산감시 수집 작업 종료 일시',
    `CHAM_MONIMAP_COLLECT_JOB_POSTS_SEEN`    INT           NOT NULL    DEFAULT 0 COMMENT '자치연대 예산감시 수집 작업 확인 게시물 수',
    `CHAM_MONIMAP_COLLECT_JOB_FILES_NEW`     INT           NOT NULL    DEFAULT 0 COMMENT '자치연대 예산감시 수집 작업 신규 파일 수',
    `CHAM_MONIMAP_COLLECT_JOB_FILES_SKIPPED` INT           NOT NULL    DEFAULT 0 COMMENT '자치연대 예산감시 수집 작업 건너뛴 파일 수',
    `CHAM_MONIMAP_COLLECT_JOB_FILES_FAILED`  INT           NOT NULL    DEFAULT 0 COMMENT '자치연대 예산감시 수집 작업 실패 파일 수',
    `CHAM_MONIMAP_COLLECT_JOB_LOG`           MEDIUMTEXT    NULL        COMMENT '자치연대 예산감시 수집 작업 로그',
    `REGIST_DATE`                            DATETIME      NULL        COMMENT '생성 일시',
    `MODIFY_DATE`                            DATETIME      NULL        COMMENT '수정 일시',
    PRIMARY KEY (CHAM_MONIMAP_COLLECT_JOB_ID),
    KEY IX_CHAM_MONIMAP_COLLECT_JOB_STATUS (CHAM_MONIMAP_COLLECT_JOB_STATUS, CHAM_MONIMAP_COLLECT_JOB_ID)
);

ALTER TABLE CHAM.CHAM_MONIMAP_COLLECT_JOB COMMENT '자치연대_예산감시_수집_작업';


-- CHAM_MONIMAP_COLLECT_FILE Table Create SQL
CREATE TABLE CHAM.CHAM_MONIMAP_COLLECT_FILE
(
    `CHAM_MONIMAP_COLLECT_FILE_ID`           BIGINT          NOT NULL    AUTO_INCREMENT COMMENT '자치연대 예산감시 수집 파일 ID',
    `CHAM_MONIMAP_COLLECT_SOURCE_ID`         BIGINT          NOT NULL    COMMENT '자치연대 예산감시 수집 출처 ID',
    `CHAM_MONIMAP_COLLECT_JOB_ID`            BIGINT          NULL        COMMENT '자치연대 예산감시 수집 작업 ID (수집한 작업)',
    `CHAM_MONIMAP_COLLECT_FILE_YEAR`         INT             NULL        COMMENT '자치연대 예산감시 수집 파일 대상 연도 (해석 실패시 NULL)',
    `CHAM_MONIMAP_COLLECT_FILE_MONTH`        INT             NULL        COMMENT '자치연대 예산감시 수집 파일 대상 월 (해석 실패시 NULL)',
    `CHAM_MONIMAP_COLLECT_FILE_POST_KEY`     VARCHAR(100)    NOT NULL    COMMENT '자치연대 예산감시 수집 파일 게시물 키 (nttId/uid/wr_id/ntatcSeq)',
    `CHAM_MONIMAP_COLLECT_FILE_POST_TITLE`   VARCHAR(500)    NULL        COMMENT '자치연대 예산감시 수집 파일 게시물 제목',
    `CHAM_MONIMAP_COLLECT_FILE_POST_DATE`    DATE            NULL        COMMENT '자치연대 예산감시 수집 파일 게시일',
    `CHAM_MONIMAP_COLLECT_FILE_DETAIL_URL`   VARCHAR(1000)   NOT NULL    COMMENT '자치연대 예산감시 수집 파일 상세 URL',
    `CHAM_MONIMAP_COLLECT_FILE_ATTACH_KEY`   VARCHAR(300)    NOT NULL    COMMENT '자치연대 예산감시 수집 파일 첨부 키',
    `CHAM_MONIMAP_COLLECT_FILE_SOURCE_URL`   VARCHAR(1000)   NOT NULL    COMMENT '자치연대 예산감시 수집 파일 다운로드 URL',
    `CHAM_MONIMAP_COLLECT_FILE_ORIGIN_NAME`  VARCHAR(500)    NOT NULL    COMMENT '자치연대 예산감시 수집 파일 원본 파일명',
    `CHAM_MONIMAP_COLLECT_FILE_EXT`          VARCHAR(10)     NOT NULL    COMMENT '자치연대 예산감시 수집 파일 확장자',
    `CHAM_MONIMAP_COLLECT_FILE_S3_KEY`       VARCHAR(500)    NOT NULL    COMMENT '자치연대 예산감시 수집 파일 S3 키',
    `CHAM_MONIMAP_COLLECT_FILE_SHA256`       CHAR(64)        NOT NULL    COMMENT '자치연대 예산감시 수집 파일 SHA-256',
    `CHAM_MONIMAP_COLLECT_FILE_SIZE`         BIGINT          NOT NULL    COMMENT '자치연대 예산감시 수집 파일 크기(byte)',
    `CHAM_MONIMAP_COLLECT_FILE_COLLECTED_AT` DATETIME        NOT NULL    COMMENT '자치연대 예산감시 수집 파일 수집 일시',
    `CHAM_MONIMAP_COLLECT_FILE_STATUS`       VARCHAR(20)     NOT NULL    COMMENT '자치연대 예산감시 수집 파일 상태 (COLLECTED/DUPLICATE/IMPORTED/IGNORED/FAILED)',
    `CHAM_MONIMAP_COLLECT_FILE_DUP_OF_ID`    BIGINT          NULL        COMMENT '자치연대 예산감시 수집 파일 중복 원본 파일 ID',
    `CHAM_MONIMAP_COLLECT_FILE_DELKEY`       VARCHAR(1000)   NULL        COMMENT '자치연대 예산감시 수집 파일 반영 삭제키 (CARD_USE_DELKEY)',
    `CHAM_MONIMAP_COLLECT_FILE_IMPORT_AT`    DATETIME        NULL        COMMENT '자치연대 예산감시 수집 파일 반영 일시',
    `CHAM_MONIMAP_COLLECT_FILE_IMPORT_BY`    BIGINT          NULL        COMMENT '자치연대 예산감시 수집 파일 반영 회원 ID',
    `CHAM_MONIMAP_COLLECT_FILE_ERROR`        TEXT            NULL        COMMENT '자치연대 예산감시 수집 파일 오류 메시지',
    `REGIST_DATE`                            DATETIME        NULL        COMMENT '생성 일시',
    `MODIFY_DATE`                            DATETIME        NULL        COMMENT '수정 일시',
    PRIMARY KEY (CHAM_MONIMAP_COLLECT_FILE_ID),
    UNIQUE KEY UK_CHAM_MONIMAP_COLLECT_FILE_ATTACH (CHAM_MONIMAP_COLLECT_SOURCE_ID, CHAM_MONIMAP_COLLECT_FILE_POST_KEY, CHAM_MONIMAP_COLLECT_FILE_ATTACH_KEY),
    KEY IX_CHAM_MONIMAP_COLLECT_FILE_SHA256 (CHAM_MONIMAP_COLLECT_FILE_SHA256),
    KEY IX_CHAM_MONIMAP_COLLECT_FILE_PERIOD (CHAM_MONIMAP_COLLECT_SOURCE_ID, CHAM_MONIMAP_COLLECT_FILE_YEAR, CHAM_MONIMAP_COLLECT_FILE_MONTH),
    KEY IX_CHAM_MONIMAP_COLLECT_FILE_STATUS (CHAM_MONIMAP_COLLECT_FILE_STATUS)
);

ALTER TABLE CHAM.CHAM_MONIMAP_COLLECT_FILE COMMENT '자치연대_예산감시_수집_파일';

ALTER TABLE CHAM.CHAM_MONIMAP_COLLECT_FILE
    ADD CONSTRAINT FK_CHAM_MONIMAP_COLLECT_FILE_SOURCE FOREIGN KEY (CHAM_MONIMAP_COLLECT_SOURCE_ID)
        REFERENCES CHAM.CHAM_MONIMAP_COLLECT_SOURCE (CHAM_MONIMAP_COLLECT_SOURCE_ID) ON DELETE RESTRICT ON UPDATE RESTRICT;


-- ─────────────────────────────────────────────────────────────
-- 대전시장은 첨부 중 시장·부시장 파일만 받는다(요약표 '사용내역공개', '정무…' 는 뺀다). 시드 뒤 UPDATE 참고.
-- 출처 12곳 시드. 서구의회(게시판 비어 있음)를 뺀 11곳을 ENABLED=1. PDF 4곳(대전시·대전시의회·유성구청장·대덕구청장)은
-- 원본 보관과 PDF 미리보기만 된다(반영은 엑셀만).
--
-- !! 넣기 전에 운영 DB 에서 두 가지를 확인할 것 !!
--   1) POSITION_NAME 이 CHAM_MONIMAP_CARD_OWNER_POSITION_NAME 에 실제로 있는 값인지.
--      반영(import) 때 없는 직위면 새로 만들지 않고 거부한다.
--      2026-09-24 운영 DB 기준: 국회의원 / 광역의회 / 기초의회 / 기타 / 기초지자체 / 광역지자체
--        SELECT CHAM_MONIMAP_CARD_OWNER_POSITION_NAME FROM CHAM.CHAM_MONIMAP_CARD_OWNER_POSITION;
--   2) REGION 표기가 기존 자료와 같은지.
--        SELECT DISTINCT CHAM_MONIMAP_CARD_USE_REGION FROM CHAM.CHAM_MONIMAP_CARD_USE;
--   DEFAULT_USER 는 엑셀 2열 '사용자'(직함) 기본값이다. 원본에 사용자 칸이 없고 시트 이름도 직함이 아닐 때 쓴다.
--   DEFAULT_NAME 은 엑셀 3열 '이름'(사람 이름) 기본값이다. 기존 자료는 사용자=직함(구청장/의장),
--   이름=사람 이름(박희조/오은규)으로 들어가 있다. 단체장은 한 사람이라 여기 이름을 넣고,
--   의회는 행마다 직함이 달라 비워둔다. 의회 자료는 반영할 때 같은 지역·같은 직함의
--   기존 자료에서 가장 최근 이름을 찾아 채운다. 단체장이 바뀌면 이 값을 고칠 것.
-- ─────────────────────────────────────────────────────────────
INSERT INTO CHAM.CHAM_MONIMAP_COLLECT_SOURCE
    (CHAM_MONIMAP_COLLECT_SOURCE_CODE, CHAM_MONIMAP_COLLECT_SOURCE_NAME, CHAM_MONIMAP_COLLECT_SOURCE_ENGINE,
     CHAM_MONIMAP_COLLECT_SOURCE_LIST_URL, CHAM_MONIMAP_COLLECT_SOURCE_DETAIL_URL, CHAM_MONIMAP_COLLECT_SOURCE_BOARD_ID,
     CHAM_MONIMAP_COLLECT_SOURCE_PAGE_PARAM, CHAM_MONIMAP_COLLECT_SOURCE_EXTRA_PARAM,
     CHAM_MONIMAP_COLLECT_SOURCE_POSITION_NAME, CHAM_MONIMAP_COLLECT_SOURCE_REGION, CHAM_MONIMAP_COLLECT_SOURCE_DEFAULT_USER, CHAM_MONIMAP_COLLECT_SOURCE_DEFAULT_NAME,
     CHAM_MONIMAP_COLLECT_SOURCE_FILE_FORMAT, CHAM_MONIMAP_COLLECT_SOURCE_ALLOW_EXT, CHAM_MONIMAP_COLLECT_SOURCE_ENABLED,
     CHAM_MONIMAP_COLLECT_SOURCE_SORT, CHAM_MONIMAP_COLLECT_SOURCE_NOTE, REGIST_DATE, MODIFY_DATE)
VALUES
    ('DAEJEON_MAYOR', '대전광역시장·부시장', 'CUSTOM_DAEJEON',
     'https://www.daejeon.go.kr/drh/open/drhDataOpen/drhDataOpenBoardView.do?boardSeq=1186&menuSeq=4804',
     'https://www.daejeon.go.kr/drh/open/drhDataOpen/drhDataOpenBoardArticleView.do?menuSeq=4804&boardSeq=1186&articleSeq={postKey}&subPageIndex=1',
     '1186', 'subPageIndex', NULL, '광역지자체', '대전', '시장', '허태정', 'PDF', 'pdf', 1, 10,
     '원래 3단. 목록을 시장·부시장 항목(boardSeq=1186)으로 잡아 2단으로 쓴다. 첨부 fileDownLoad(경로) → /{경로}', NOW(), NOW()),
    ('DAEJEON_COUNCIL', '대전시의회', 'COUNCIL_A',
     'https://council.daejeon.go.kr/svc/inf/OperatingExpenseList.do',
     'https://council.daejeon.go.kr/svc/inf/OperatingExpenseView.do?bbsSn={postKey}', NULL,
     'pageNo', NULL, '광역의회', '대전', NULL, NULL, 'PDF', 'pdf', 1, 20,
     '목록은 GET ?pageNo= 로 열림. 한 달에 위원회별로 여러 건. 첨부 /bbs/FileDownLoadProc.do?flSn=', NOW(), NOW()),
    ('DONGGU_MAYOR', '동구청장', 'CUSTOM_DONGGU',
     'https://www.donggu.go.kr/dg/kor/article/secretBusiness',
     'https://www.donggu.go.kr/dg/kor/article/secretBusiness/{postKey}', NULL,
     'pageIndex', NULL, '기초지자체', '대전 동구', '구청장', '박희조', 'XLSX', 'xlsx,xls', 1, 30,
     '목록은 article.view(seq) JS. 상세는 /secretBusiness/{seq} GET. 장소 컬럼 없음', NOW(), NOW()),
    ('DONGGU_COUNCIL', '동구의회', 'COUNCIL_B',
     'https://council.donggu.go.kr/kr/activity/bbs?bbs_id=cost',
     'https://council.donggu.go.kr/kr/activity/bbs?bbs_id=cost&reform=view&uid={postKey}', 'cost',
     'page', NULL, '기초의회', '대전 동구', NULL, NULL, 'XLSX', 'xlsx,xls', 1, 40,
     '의회B 계열, URL 스킨만 다름', NOW(), NOW()),
    ('SEOGU_MAYOR', '서구청장', 'EGOV_BBS',
     'https://www.seogu.go.kr/bbs/BBSMSTR_000000000571/list.do',
     'https://www.seogu.go.kr/bbs/BBSMSTR_000000000571/view.do?nttId={postKey}', 'BBSMSTR_000000000571',
     'pageIndex', NULL, '기초지자체', '대전 서구', '구청장', '서철모', 'XLSX_PDF', 'xlsx,xls', 1, 50,
     'PDF 와 xlsx 둘 다 올림. xlsx 만 수집', NOW(), NOW()),
    ('SEOGU_COUNCIL', '서구의회', 'COUNCIL_A',
     'https://www.seogucouncil.daejeon.kr/svc/cdr/OperatingExpenseList.do', NULL, NULL,
     'pageNo', NULL, '기초의회', '대전 서구', NULL, NULL, 'NONE', 'xlsx,xls', 0, 60,
     '게시판 비어있음(2026-09-24 재확인). 사람이 확인하거나 의회에 문의 필요', NOW(), NOW()),
    ('JUNGGU_MAYOR', '중구청장', 'EGOV_BBS',
     'https://www.djjunggu.go.kr/bbs/BBSMSTR_000000000103/list.do',
     'https://www.djjunggu.go.kr/bbs/BBSMSTR_000000000103/view.do?nttId={postKey}', 'BBSMSTR_000000000103',
     'pageIndex', NULL, '기초지자체', '대전 중구', '구청장', '김제선', 'XLSX', 'xlsx,xls', 1, 70,
     '시트 2개(기관운영/시책추진). 장소 컬럼 없음', NOW(), NOW()),
    ('JUNGGU_COUNCIL', '중구의회', 'COUNCIL_B',
     'https://council.djjunggu.go.kr/kr/costBBS.do',
     'https://council.djjunggu.go.kr/kr/costBBSview.do?uid={postKey}', NULL,
     'page', NULL, '기초의회', '대전 중구', NULL, NULL, 'XLSX', 'xlsx,xls', 1, 80,
     '가맹점명·주소 있음. 사용일시 한 칸', NOW(), NOW()),
    ('YUSEONG_MAYOR', '유성구청장', 'EGOV_BBS',
     'https://www.yuseong.go.kr/bbs/BBSMSTR_000000000111/list.do',
     'https://www.yuseong.go.kr/bbs/BBSMSTR_000000000111/view.do?nttId={postKey}', 'BBSMSTR_000000000111',
     'pageIndex', NULL, '기초지자체', '대전 유성구', '구청장', '정용래', 'PDF', 'pdf', 1, 90,
     'PDF 만 올림. 원본 보관용으로 받는다(반영은 사람이 정리해 수동 업로드)', NOW(), NOW()),
    ('YUSEONG_COUNCIL', '유성구의회', 'GNUBOARD',
     'https://yuseonggucouncil.go.kr/bbs/board.php?bo_table=0603',
     'https://yuseonggucouncil.go.kr/bbs/board.php?bo_table=0603&wr_id={postKey}', '0603',
     'page', NULL, '기초의회', '대전 유성구', NULL, NULL, 'XLSX', 'xlsx,xls', 1, 100,
     '그누보드', NOW(), NOW()),
    ('DAEDEOK_MAYOR', '대덕구청장', 'CUSTOM_DAEDEOK',
     'https://www.daedeok.go.kr/dpt/dpt02/DPT02010401_cmmBoardList.do',
     'https://www.daedeok.go.kr/dpt/dpt02/DPT02010401_cmmBoardView.do?boardId=DPT_000022&ntatcSeq={postKey}', 'DPT_000022',
     'pageIndex', NULL, '기초지자체', '대전 대덕구', '구청장', '최충규', 'PDF', 'xlsx,xls,pdf', 1, 110,
     '2026년 8월분부터 PDF 만 올림(2026-09-24 확인). 원본 보관용으로 받는다', NOW(), NOW()),
    ('DAEDEOK_COUNCIL', '대덕구의회', 'COUNCIL_B',
     'https://council.daedeok.go.kr/kr/costBBS.do',
     'https://council.daedeok.go.kr/kr/costBBSview.do?uid={postKey}', NULL,
     'page', NULL, '기초의회', '대전 대덕구', NULL, NULL, 'XLSX', 'xlsx,xls', 1, 120,
     '의회B 계열', NOW(), NOW());

-- 대전시: 한 달치에 PDF 가 여럿(0. 사용내역공개 / 1. 시장 / 2. 행정부시장 / 3. 정무…) 붙는다. 시장·부시장만 받는다
UPDATE CHAM.CHAM_MONIMAP_COLLECT_SOURCE
SET CHAM_MONIMAP_COLLECT_SOURCE_ATTACH_INCLUDE = '시장', CHAM_MONIMAP_COLLECT_SOURCE_ATTACH_EXCLUDE = '정무'
WHERE CHAM_MONIMAP_COLLECT_SOURCE_CODE = 'DAEJEON_MAYOR';
