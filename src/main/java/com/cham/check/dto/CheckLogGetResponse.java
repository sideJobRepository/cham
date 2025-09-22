package com.cham.check.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CheckLogGetResponse {

    private Long chamMonimapCardUseAddrId;
    private Long visitedCnt;
    private Long suspiciousedCnt;
    
    @QueryProjection
    public CheckLogGetResponse(Long chamMonimapCardUseAddrId, Long visitedCnt, Long suspiciousedCnt) {
        this.chamMonimapCardUseAddrId = chamMonimapCardUseAddrId;
        this.visitedCnt = visitedCnt;
        this.suspiciousedCnt = suspiciousedCnt;
    }
}
