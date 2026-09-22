package com.cham.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 삭제키 이름 변경 요청.
 *
 * 삭제키는 별도 테이블 없이 CHAM_MONIMAP_CARD_USE 행마다 들어 있는 값이라
 * 이름 변경도 그 업로드에 속한 행을 전부 바꾸는 벌크 UPDATE 가 된다.
 */
@Getter
@Setter
@NoArgsConstructor
public class CardUseUploadDeleteKeyRequest {

    @NotBlank(message = "삭제키는 필수입니다.")
    private String deleteKey;

    // 컬럼이 VARCHAR(1000) 이지만 인덱스를 앞 255자로만 잡아 뒀다.
    // 그보다 길면 서로 다른 키가 같은 인덱스 구간에 몰려 조회가 느려진다.
    @NotBlank(message = "새 삭제키는 필수입니다.")
    @Size(max = 255, message = "새 삭제키는 255자를 넘을 수 없습니다.")
    private String newDeleteKey;
}
