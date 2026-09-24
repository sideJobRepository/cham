package com.cham.collector.domain;

/** 게시판 엔진. 12곳이지만 어댑터는 이 종류만큼이면 된다 */
public enum EngineType {
    EGOV_BBS,        // 전자정부 표준 게시판 (서구청·중구청·유성구청)
    COUNCIL_B,       // kr/costBBS.do 계열 (중구의회·대덕구의회·동구의회)
    COUNCIL_A,       // svc/.../OperatingExpenseList.do (대전시의회·서구의회)
    GNUBOARD,        // 유성구의회
    CUSTOM_DONGGU,   // 동구청
    CUSTOM_DAEDEOK,  // 대덕구청
    CUSTOM_DAEJEON   // 대전시 (PDF)
}
