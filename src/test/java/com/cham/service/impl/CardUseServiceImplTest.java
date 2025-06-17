package com.cham.service.impl;

import com.cham.RepositoryAndServiceTestSupport;
import com.cham.controller.request.CardUseConditionRequest;
import com.cham.controller.response.CardUseResponse;
import com.cham.service.CardUseService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.Map;

class CardUseServiceImplTest extends RepositoryAndServiceTestSupport {
    
    
    
    @Autowired
    private CardUseService cardUseService;
    
    
    @DisplayName("")
    @Test
    void test(){
        
        CardUseConditionRequest cardUseConditionRequest = new CardUseConditionRequest(null,null,3, LocalDate.of(2022,1,1),null);
        
        Map<Long, CardUseResponse> responseMap =  cardUseService.selectCardUse(cardUseConditionRequest);
    }
}