package com.cham.check.repository.query;

import com.cham.check.dto.response.CheckLogGetResponse;
import com.cham.check.entity.ChamMonimapCheckLog;

public interface ChamMonimapCheckLogQueryRepository {
    
    CheckLogGetResponse findByCheckAggregation(Long chamMonimapCardId,Long memberId);
    
    
    ChamMonimapCheckLog findByCheckLog(Long memberId,Long addrId);
}
