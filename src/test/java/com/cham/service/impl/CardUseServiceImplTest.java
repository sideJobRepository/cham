package com.cham.service.impl;

import com.cham.RepositoryAndServiceTestSupport;
import com.cham.caruse.dto.CardUseAggregateResponse;
import com.cham.caruse.service.ChamMonimapCardUseService;
import com.cham.dto.request.CardUseConditionRequest;
import com.cham.dto.response.CardUseResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.Map;
import java.util.stream.Collectors;

class CardUseServiceImplTest extends RepositoryAndServiceTestSupport {
    
    
    
    @Autowired
    private ChamMonimapCardUseService cardUseService;
    
    
    @DisplayName("")
    @Test
    void test(){
        LocalDate startDate = LocalDate.of(2022, 1, 1);
        CardUseConditionRequest cardUseConditionRequest = new CardUseConditionRequest();
        CardUseAggregateResponse  responseMap =  cardUseService.selectCardUse(cardUseConditionRequest);
        Map<Long, CardUseResponse> details = responseMap.getDetails();
        Map<Long, CardUseResponse> collect = details
                .entrySet()
                .stream()
                .filter(item -> !item.getValue().getColor().isEmpty())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        System.out.println("responseMap = " + collect);
    }
    
    
    @DisplayName("")
    @Test
    void test2(){
        CardUseAggregateResponse longCardUseResponseMap = cardUseService.selectCardUseDetail("대전 서구 만년남로3번길 59");
        System.out.println("longCardUseResponseMap = " + longCardUseResponseMap);
        
    }
}