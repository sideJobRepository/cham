package com.cham.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 업로드(삭제키) 단위 공개/비공개 전환 요청.
 * 삭제키를 경로가 아니라 본문으로 받는다. '2025년 대전시장 5월까지 업무추진비'처럼
 * 공백과 한글이 들어간 값이라 URL 에 싣으면 인코딩 사고가 나기 쉽다.
 */
@Getter
@Setter
@NoArgsConstructor
public class CardUseUploadPublicRequest {

    @NotBlank(message = "삭제키는 필수입니다.")
    private String deleteKey;

    @NotNull(message = "공개 여부는 필수입니다.")
    private Boolean isPublic;
}
