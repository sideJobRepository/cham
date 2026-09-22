package com.cham.caruse;

import java.util.List;

/**
 * 자료정리에서 '손봐야 할 값' 을 가려내는 기준.
 *
 * 쿼리(정렬·필터)와 화면에 내려보내는 배지가 같은 기준을 봐야 해서 여기 한곳에 둔다.
 * 한쪽만 고치면 목록에서는 위로 올라오는데 배지는 안 붙는 식으로 어긋난다.
 *
 * 어디까지나 눈에 띄게 해주는 장치일 뿐 완전하지 않다. 'ㅁㄴㅇㄹ' 처럼 아무 단어도
 * 안 걸리는 값은 못 잡는다. 그래서 목록에서 걸러 숨기지 않고 전체를 보여준 뒤
 * 위로만 올린다.
 */
public final class CleanupRules {

    // 사람이 메모처럼 적어 넣은 흔적
    public static final List<String> ADDR_NAME_WORDS = List.of("모르", "미상", "확인", "없", "?");
    public static final List<String> MEMBER_NAME_WORDS = List.of("(", "（", "?", "모르", "미상");

    public static boolean isAddrNameSuspicious(String addrName) {
        return containsAny(addrName, ADDR_NAME_WORDS);
    }

    public static boolean isMemberNameSuspicious(String memberName) {
        return containsAny(memberName, MEMBER_NAME_WORDS);
    }

    private static boolean containsAny(String value, List<String> words) {
        if (value == null) return false;
        return words.stream().anyMatch(value::contains);
    }

    private CleanupRules() {
    }
}
