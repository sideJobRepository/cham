package com.cham.collect.parser;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 수집 원본의 헤더 이름 → 칸 사전.
 *
 * 기관마다 같은 칸을 다른 말로 부른다(사용일자/집행일자, 사용방법/결제방법).
 * 새 기관 파일에서 헤더를 못 찾으면 코드가 아니라 여기 단어를 더한다.
 * 미리보기 화면의 '매핑 안 된 헤더' 가 무엇을 더해야 할지 알려준다.
 */
public final class CollectHeaderRules {

    // 앞쪽 필드가 먼저 이긴다. 같은 필드 안에서는 앞쪽 단어가 먼저다.
    public static final Map<CollectField, List<String>> SYNONYMS;

    static {
        Map<CollectField, List<String>> m = new LinkedHashMap<>();
        m.put(CollectField.SEQ, List.of("연번", "순번", "번호"));
        m.put(CollectField.DATETIME, List.of("사용일시", "집행일시", "일시"));
        m.put(CollectField.DATE, List.of("사용일자", "집행일자", "사용일", "집행일", "일자", "날짜", "사용날짜"));
        m.put(CollectField.TIME, List.of("사용시간", "집행시간", "시간"));
        m.put(CollectField.USER, List.of("사용자", "집행자", "사용부서", "집행부서"));
        m.put(CollectField.NAME, List.of("성명", "이름"));
        m.put(CollectField.ADDR_NAME, List.of("사용장소", "가맹점명", "집행장소", "결제장소", "사용처", "장소", "상호", "상호명", "업소명", "가맹점"));
        m.put(CollectField.ADDR_DETAIL, List.of("가맹점주소", "사용장소주소", "상세주소", "주소", "소재지"));
        m.put(CollectField.PURPOSE, List.of("사용목적", "집행목적", "사용내역", "집행내역", "목적", "내역", "내용"));
        m.put(CollectField.PERSONNEL, List.of("대상인원", "집행대상인원", "인원", "대상자수"));
        m.put(CollectField.AMOUNT, List.of("사용금액", "집행금액", "금액", "사용액", "집행액"));
        m.put(CollectField.METHOD, List.of("사용방법", "결제방법", "결제방식", "집행방법", "결제수단", "지출방법"));
        m.put(CollectField.REMARK, List.of("비고"));
        m.put(CollectField.POSITION, List.of("직위", "직책", "직급"));
        SYNONYMS = Collections.unmodifiableMap(m);
    }

    // 표 맨 아래 합계 줄
    public static final Set<String> TOTAL_ROW_WORDS = Set.of("계", "합계", "소계", "총계", "누계", "총합계");

    // 제목·기관명·작성일 같은 머리글이 표 위에 몇 줄 붙어 있다. 이 안에서 헤더를 찾는다
    public static final int HEADER_SCAN_ROWS = 15;

    // 이만큼 칸이 잡혀야 헤더 줄로 본다. 제목 줄에 '업무추진비 사용내역' 같은 말이 걸려도 넘어가지 않게
    public static final int MIN_HEADER_HITS = 3;

    /** 공백·줄바꿈·괄호 안 내용을 지운다. '사용금액(원)' → '사용금액', '가맹점 주소' → '가맹점주소' */
    public static String normalize(String raw) {
        if (raw == null) return "";
        return raw
                .replaceAll("[(\\[（][^)\\]）]*[)\\]）]", "")
                .replaceAll("[\\s\\u00A0\\u3000*※:·]", "")
                .trim();
    }

    /** 헤더 한 칸이 어느 필드인지. 완전히 같은 단어를 먼저 보고, 없으면 앞부분이 같은 단어를 본다 */
    public static Optional<CollectField> match(String header) {
        String h = normalize(header);
        if (h.isEmpty()) return Optional.empty();

        for (Map.Entry<CollectField, List<String>> e : SYNONYMS.entrySet()) {
            if (e.getValue().contains(h)) return Optional.of(e.getKey());
        }
        // '사용금액원' 처럼 괄호 없이 단위가 붙은 경우. 긴 단어부터 봐야 '가맹점' 이 '가맹점주소' 를 먹지 않는다
        CollectField best = null;
        int bestLen = 0;
        for (Map.Entry<CollectField, List<String>> e : SYNONYMS.entrySet()) {
            for (String word : e.getValue()) {
                if (word.length() >= 2 && h.startsWith(word) && word.length() > bestLen) {
                    best = e.getKey();
                    bestLen = word.length();
                }
            }
        }
        return Optional.ofNullable(best);
    }

    public static boolean isTotalWord(String raw) {
        return TOTAL_ROW_WORDS.contains(normalize(raw));
    }

    // 사용자(직함) 칸이 없는 파일은 시트 이름이 직함이다(동구의회 '의장', '부의장' 시트).
    // 다만 'Sheet1', '업무추진비(구청장)_8월', '⊙' 처럼 직함이 아닌 이름이면 기관 기본 직함을 쓴다
    private static final Pattern GENERIC_SHEET = Pattern.compile("(?i)^sheet\\d*$|업무추진비|\\d+\\s*월|집행내역|사용내역");
    private static final Pattern HANGUL = Pattern.compile("[가-힣]");

    public static boolean isRoleSheetName(String sheetName) {
        if (sheetName == null) return false;
        String n = sheetName.trim();
        return n.length() >= 2 && n.length() <= 20 && HANGUL.matcher(n).find() && !GENERIC_SHEET.matcher(n).find();
    }

    // '금강휴게소 (충북 옥천군 동이면 금강로 596)' 처럼 장소명 뒤 괄호에 주소를 넣는 기관이 있다(중구청)
    private static final Pattern PLACE_WITH_ADDR = Pattern.compile("^(.+?)\\s*[(（]([^()（）]+)[)）]\\s*$");
    private static final Pattern ADDR_LIKE = Pattern.compile("[가-힣]+(시|군|구|동|읍|면|로|길)(\\s|\\d|$)");

    /** 장소명 괄호 안이 주소처럼 보이면 [장소명, 주소] 로 나눈다. 아니면 null */
    public static String[] splitPlaceAndAddr(String addrName) {
        if (addrName == null) return null;
        Matcher m = PLACE_WITH_ADDR.matcher(addrName.trim());
        if (!m.matches()) return null;
        String inner = m.group(2).trim();
        if (!ADDR_LIKE.matcher(inner).find() || !inner.matches(".*\\d.*|.*(동|구|군)$")) return null;
        return new String[]{m.group(1).trim(), inner};
    }

    private CollectHeaderRules() {
    }
}
