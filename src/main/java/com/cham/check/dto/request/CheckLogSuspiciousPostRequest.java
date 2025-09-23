package com.cham.check.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CheckLogSuspiciousPostRequest {
    private Long memberId;
    private Long addrId;
    private String suspicioused;
}
