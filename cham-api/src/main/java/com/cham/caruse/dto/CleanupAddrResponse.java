package com.cham.caruse.dto;

import com.cham.caruse.CardUseDefaults;
import com.cham.caruse.CleanupRules;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 자료정리 화면의 장소 한 줄.
 * 지도에 그대로 노출되고 핀 위치까지 정하는 값이라 여기부터 고쳐야 한다.
 */
@Getter
@RequiredArgsConstructor
public class CleanupAddrResponse {

    private final Long addrId;
    private final String addrName;
    private final String detailAddr;

    // 좌표. 비어 있으면 지도에 핀을 못 찍는다.
    private final String x;
    private final String y;

    // 이 장소로 잡힌 사용 건수. 고쳤을 때 몇 건이 함께 바뀌는지 알려준다.
    private final Long useCount;

    public boolean isNoCoordinate() {
        return x == null || x.isBlank() || y == null || y.isBlank();
    }

    // 상세주소가 자리값 그대로면 실제 위치가 아니다. 핀이 엉뚱한 곳에 박혀 있다는 뜻이다.
    public boolean isFallbackAddr() {
        return CardUseDefaults.DETAIL_ADDR.equals(detailAddr);
    }

    // 장소명에 '어딘지 모르겠어요' 처럼 메모가 남아 있는 경우
    public boolean isSuspiciousName() {
        return CleanupRules.isAddrNameSuspicious(addrName);
    }

    // 셋 중 하나라도 걸리면 손봐야 할 줄이다. 목록에서 위로 올라오는 기준과 같다.
    public boolean isIssue() {
        return isNoCoordinate() || isFallbackAddr() || isSuspiciousName();
    }
}
