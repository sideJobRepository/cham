package com.cham.check.service;

import com.cham.check.dto.CheckLogGetResponse;

public interface ChamMonimapCheckLogService {
    
    CheckLogGetResponse findByCheckAggregation(Long chamMonimapCardUseAddrId);
}
