package com.cham.check.repository.query;

import com.cham.check.dto.CheckLogGetResponse;

public interface ChamMonimapCheckLogQueryRepository {
    
    CheckLogGetResponse findByCheckAggregation(Long chamMonimapCardId);
}
