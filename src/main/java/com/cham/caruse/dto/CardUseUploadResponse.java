package com.cham.caruse.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 관리자 공개관리 화면의 한 줄.
 * 엑셀 업로드 한 건 = 삭제키 하나라서 삭제키로 묶은 결과가 그대로 목록이 된다.
 */
// final 필드만 받는 생성자를 만든다. Projections.constructor 가 이 생성자를 그대로 쓰므로
// members 처럼 나중에 끼워 넣는 항목이 생성자에 끼어들면 안 된다.
@Getter
@RequiredArgsConstructor
public class CardUseUploadResponse {

    // 업로드 식별자. 엑셀 2행 14열에 적힌 값 그대로다.
    private final String deleteKey;

    // 대표 기관(직위). 한 업로드에 여러 기관이 섞이면 이름이 가장 앞선 것 하나만 보여준다.
    private final String positionName;

    // 이 업로드로 들어온 사용 건수
    private final Long useCount;

    // 사용일자 범위. 어느 시기 자료인지 목록에서 바로 구분하려고 같이 내린다.
    private final LocalDate firstUseDate;
    private final LocalDate lastUseDate;

    // 업로드 시각
    private final LocalDateTime registDate;

    // 지도 공개 여부
    private final Boolean isPublic;

    // 이 업로드에 들어 있는 사용자 명단.
    // 한 번에 집계할 수 없어 별도 쿼리로 받아 서비스에서 끼워 넣는다.
    // 그래서 위 항목들과 달리 final 이 아니고 생성자에도 들어가지 않는다
    // (생성자는 Projections.constructor 가 그대로 쓴다).
    @Setter
    private List<CardUseUploadMemberResponse> members = List.of();
}
