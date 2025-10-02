package com.cham.service.impl;

import com.cham.RepositoryAndServiceTestSupport;
import com.cham.dto.request.CardUseConditionRequest;
import com.cham.dto.response.CardUseResponse;
import com.cham.caruse.service.ChamMonimapCardUseService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.Map;

class CardUseServiceImplTest extends RepositoryAndServiceTestSupport {
    
    
    
    @Autowired
    private ChamMonimapCardUseService cardUseService;
    
    
    @DisplayName("")
    @Test
    void test(){
        LocalDate startDate = LocalDate.of(2022, 1, 1);
        CardUseConditionRequest cardUseConditionRequest = new CardUseConditionRequest();
        
        Map<Long, CardUseResponse> responseMap =  cardUseService.selectCardUse(cardUseConditionRequest);
        System.out.println("responseMap = " + responseMap);
    }
    
    
    @DisplayName("")
    @Test
    void test2(){
        Map<Long, CardUseResponse> longCardUseResponseMap = cardUseService.selectCardUseDetail("중구 대흥로 131");
        System.out.println("longCardUseResponseMap = " + longCardUseResponseMap);
        
    }
}