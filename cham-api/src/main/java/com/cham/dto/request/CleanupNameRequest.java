package com.cham.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 이름 정리 요청.
 * 같은 표기를 쓰는 행을 전부 새 표기로 바꾼다. 이미 있는 표기로 바꾸면 두 묶음이 하나로 합쳐진다.
 */
@Getter
@Setter
@NoArgsConstructor
public class CleanupNameRequest {

    @NotBlank(message = "기존 이름은 필수입니다.")
    private String oldName;

    @NotBlank(message = "새 이름은 필수입니다.")
    private String newName;
}
