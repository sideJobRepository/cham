package com.cham.check.service.impl;

import com.cham.RepositoryAndServiceTestSupport;
import com.cham.check.dto.CheckLogGetResponse;
import com.cham.check.service.ChamMonimapCheckLogService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

class ChamMonimapCheckLogServiceImplTest extends RepositoryAndServiceTestSupport {
    
    @Autowired
    private ChamMonimapCheckLogService chamMonimapCheckLogService;
    
    @DisplayName("")
    @Test
    void test1(){
        Long key = 1L;
        
        CheckLogGetResponse byCheckAggregation = chamMonimapCheckLogService.findByCheckAggregation(key);
        assertThat(byCheckAggregation)
                .extracting("visitedCnt")
                .isEqualTo(1L);
        
    }
}