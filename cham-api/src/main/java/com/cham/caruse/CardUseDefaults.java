package com.cham.caruse;

/**
 * 엑셀에 값이 비어 있을 때 채워 넣는 자리값.
 *
 * 실제 주소도 실제 가게 이름도 아니다. 업로드를 통과시키려고 메워둔 것이라
 * 자료정리 화면에서 '고쳐야 할 줄' 을 가려내는 기준으로도 쓴다.
 * 그래서 넣는 쪽과 찾아내는 쪽이 같은 값을 보도록 여기 한곳에 둔다.
 */
public final class CardUseDefaults {

    public static final String DETAIL_ADDR = "도산로370번길 22-1";
    public static final String ADDR_NAME = "경조사비";
    // 이름(엑셀 3열)을 모를 때. 기존 자료도 직원 몫은 '공무원' 으로 적어 왔다
    public static final String NAME = "공무원";

    private CardUseDefaults() {
    }
}
