package com.cham.collect.parser;

/** 수집 원본의 헤더가 가리킬 수 있는 칸. CardUseRow 의 필드와 거의 1:1 이다. */
public enum CollectField {
    SEQ,        // 연번. 읽지 않고 합계 줄을 가려낼 때만 본다
    DATETIME,   // 일자+시간이 한 칸에 (중구의회 '사용일시')
    DATE,
    TIME,
    USER,       // 사용자 = 직함 (구청장/의장/국장)
    NAME,       // 이름 = 사람 이름
    ADDR_NAME,  // 가맹점명
    ADDR_DETAIL,
    PURPOSE,
    PERSONNEL,
    AMOUNT,
    METHOD,
    REMARK,
    POSITION    // 파일 안의 '직위/직책'. 분류로 쓰지 않고 사용자가 비었을 때 사용자로 쓴다
}
