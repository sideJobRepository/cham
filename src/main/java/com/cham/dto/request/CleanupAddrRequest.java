package com.cham.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 장소 정리 요청.
 * 상세주소가 바뀌면 그 주소로 좌표를 다시 찾아 넣는다. 핀 위치가 같이 움직인다.
 */
@Getter
@Setter
@NoArgsConstructor
public class CleanupAddrRequest {

    @NotNull(message = "장소 ID 는 필수입니다.")
    private Long addrId;

    @NotBlank(message = "장소명은 필수입니다.")
    private String addrName;

    @NotBlank(message = "상세주소는 필수입니다.")
    private String detailAddr;
}
