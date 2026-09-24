package com.cham.caruse;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * 업로드 엑셀 한 줄. 열 구성은 ExcelColumns 14열과 같다(삭제키 제외).
 *
 * 수동 업로드(열 위치 고정)와 수집 반영(헤더 이름으로 찾음)이 파싱 방식만 다르고
 * 저장은 같은 길(insertRows)을 타도록 둘 사이에 이 모양을 둔다.
 * 자리값(CardUseDefaults)은 여기 넣지 않는다. 비어 있으면 비어 있는 채로 넘기고 저장할 때 채운다.
 */
public record CardUseRow(
        String ownerPosition,
        String region,
        String user,
        String name,
        LocalDate date,
        LocalTime time,
        String addrName,
        String addrDetail,
        String purpose,
        String personnel,
        Double amount,
        String method,
        String remark,
        // 오류 메시지에 쓰는 원본 위치. 엑셀 기준 1부터
        int sourceRowNum,
        String sheetName
) {
}
