package com.cham.check.service.impl;

import com.cham.check.dto.CheckLogGetResponse;
import com.cham.check.repository.ChamMonimapCheckLogRepository;
import com.cham.check.service.ChamMonimapCheckLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class ChamMonimapCheckLogServiceImpl implements ChamMonimapCheckLogService {

    private final ChamMonimapCheckLogRepository chamMonimapCheckLogRepository;
    
    @Override
    public CheckLogGetResponse findByCheckAggregation(Long chamMonimapCardUseAddrId) {
        CheckLogGetResponse byCheckAggregation = chamMonimapCheckLogRepository.findByCheckAggregation(chamMonimapCardUseAddrId);
        return byCheckAggregation;
    }
}
