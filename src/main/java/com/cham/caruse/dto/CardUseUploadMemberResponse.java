package com.cham.caruse.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 업로드 한 건에 들어 있는 사용자(이장우 등) 한 명.
 * 엑셀 4번째 열(이름)이 그대로 들어온 값이라 표기가 흔들리면 다른 사람으로 잡힌다.
 */
@Getter
@AllArgsConstructor
public class CardUseUploadMemberResponse {

    // 어느 업로드에 속하는지. 서버에서 묶을 때만 쓰고 이미 부모에 있는 값이라 내려보내지 않는다.
    @JsonIgnore
    private final String deleteKey;

    private final String memberName;

    // 이 사람 앞으로 잡힌 사용 건수
    private final Long useCount;
}
