package com.cham.check.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CheckLogVisitedPostRequest {
    
    private Long memberId;
    private Long addrId;
    private String visited;
    private String suspicioused;
}
