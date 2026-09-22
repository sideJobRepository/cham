package com.cham.caruse.dto;

import com.cham.caruse.CleanupRules;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 자료정리 화면의 이름 한 줄.
 *
 * 지도에는 안 나오고 관리자 명단·검색에만 쓰이지만, '최중규' 와 '최중규 (은혜식당)' 처럼
 * 표기가 갈리면 같은 사람이 둘로 세어진다.
 */
@Getter
@RequiredArgsConstructor
public class CleanupNameResponse {

    private final String memberName;

    // 이 표기로 잡힌 사용 건수
    private final Long useCount;

    // 괄호나 물음표가 붙어 있는 등 손봐야 할 표기인지. 목록에서 위로 올라오는 기준과 같다.
    public boolean isIssue() {
        return CleanupRules.isMemberNameSuspicious(memberName);
    }
}
