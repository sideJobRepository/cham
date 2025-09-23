package com.cham.check.service.impl;

import com.cham.RepositoryAndServiceTestSupport;
import com.cham.check.dto.request.CheckLogSuspiciousPostRequest;
import com.cham.check.dto.request.CheckLogVisitedPostRequest;
import com.cham.check.dto.response.CheckLogGetResponse;
import com.cham.check.service.ChamMonimapCheckLogService;
import com.cham.dto.response.ApiResponse;
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
        
        CheckLogGetResponse byCheckAggregation = chamMonimapCheckLogService.findByCheckAggregation(key, 1L);
        
        System.out.println("byCheckAggregation = " + byCheckAggregation);
//        assertThat(byCheckAggregation)
//                .extracting("visitedCnt")
//                .isEqualTo(1L);
//
    }
    
    @DisplayName("")
    @Test
    void test2(){
        CheckLogVisitedPostRequest request = CheckLogVisitedPostRequest.builder()
                .memberId(1L)
                .addrId(1L)
                .visited("Y")
                        .build();
        
        ApiResponse checkLogVisit = chamMonimapCheckLogService.createCheckLogVisit(request);
        System.out.println("checkLogVisit = " + checkLogVisit);
        
    }
    
    @DisplayName("")
    @Test
    void test3(){
        CheckLogSuspiciousPostRequest request = CheckLogSuspiciousPostRequest.builder()
                .memberId(1L)
                .addrId(1L)
                .suspicioused(null)
                .build();
        ApiResponse checkLogSuspicious = chamMonimapCheckLogService.createCheckLogSuspicious(request);
        
        System.out.println("checkLogSuspicious = " + checkLogSuspicious);
    }
    
}