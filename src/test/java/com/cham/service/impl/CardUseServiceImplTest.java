package com.cham.service.impl;

import com.cham.RepositoryAndServiceTestSupport;
import com.cham.caruse.dto.CardUseAggregateResponse;
import com.cham.caruse.service.ChamMonimapCardUseService;
import com.cham.dto.request.CardUseConditionRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;

class CardUseServiceImplTest extends RepositoryAndServiceTestSupport {
    
    
    
    @Autowired
    private ChamMonimapCardUseService cardUseService;
    
    
    @DisplayName("")
    @Test
    void test(){
        LocalDate startDate = LocalDate.of(2022, 1, 1);
        CardUseConditionRequest cardUseConditionRequest = new CardUseConditionRequest();
        cardUseConditionRequest.setInput("사이언스");
        CardUseAggregateResponse  responseMap =  cardUseService.selectCardUse(cardUseConditionRequest);
        System.out.println("responseMap = " + responseMap);
    }
    
    
    @DisplayName("")
    @Test
    void test2(){
        CardUseAggregateResponse longCardUseResponseMap = cardUseService.selectCardUseDetail("대전 서구 만년남로3번길 59");
        System.out.println("longCardUseResponseMap = " + longCardUseResponseMap);
        
    }
}