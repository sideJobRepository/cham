package com.cham.check.service;

import com.cham.check.dto.request.CheckLogSuspiciousPostRequest;
import com.cham.check.dto.request.CheckLogVisitedPostRequest;
import com.cham.check.dto.response.CheckLogGetResponse;
import com.cham.dto.response.ApiResponse;

public interface ChamMonimapCheckLogService {
    
    CheckLogGetResponse findByCheckAggregation(Long chamMonimapCardUseAddrId, Long memberId);
    
    ApiResponse createCheckLogVisit(CheckLogVisitedPostRequest request);
    ApiResponse createCheckLogSuspicious(CheckLogSuspiciousPostRequest request);
}
