package com.cham.caruse;

/**
 * insertRows 동작 선택.
 *
 * @param isPublic            저장 직후 지도 공개 여부. 수동 업로드는 공개, 수집 반영은 비공개로 넣고 공개관리에서 켠다
 * @param allowCreatePosition 모르는 직위명이 오면 새 분류를 만들지. 수집 반영에서는 막는다.
 *                            파일마다 적힌 '의장/국장' 같은 값으로 쓰레기 분류가 생기는 것을 피하려는 것이다
 */
public record CardUseInsertOptions(boolean isPublic, boolean allowCreatePosition) {

    public static final CardUseInsertOptions MANUAL_UPLOAD = new CardUseInsertOptions(true, true);
    public static final CardUseInsertOptions COLLECT_IMPORT = new CardUseInsertOptions(false, false);
}
