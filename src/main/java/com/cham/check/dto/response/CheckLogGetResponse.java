package com.cham.check.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.querydsl.core.annotations.QueryProjection;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CheckLogGetResponse {

    private Long chamMonimapCardUseAddrId;
    private Long visitedCnt;
    private Long suspiciousedCnt;
    
    private String myVisited;
    private String mySpicioused;
    
    
    @QueryProjection
    public CheckLogGetResponse(Long chamMonimapCardUseAddrId, Long visitedCnt, Long suspiciousedCnt) {
        this.chamMonimapCardUseAddrId = chamMonimapCardUseAddrId;
        this.visitedCnt = visitedCnt;
        this.suspiciousedCnt = suspiciousedCnt;
    }
    
    @QueryProjection
    public CheckLogGetResponse(Long chamMonimapCardUseAddrId, Long visitedCnt, Long suspiciousedCnt, String myVisited, String mySpicioused) {
        this.chamMonimapCardUseAddrId = chamMonimapCardUseAddrId;
        this.visitedCnt = visitedCnt;
        this.suspiciousedCnt = suspiciousedCnt;
        this.myVisited = myVisited;
        this.mySpicioused = mySpicioused;
    }
}
