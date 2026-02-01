package com.cham.feedbacck.great.service.impl;

import com.cham.RepositoryAndServiceTestSupport;
import com.cham.dto.response.ApiResponse;
import com.cham.feedbacck.great.dto.request.GreatPostRequest;
import com.cham.feedbacck.great.dto.request.GreatPutRequest;
import com.cham.feedbacck.great.dto.response.GreatResponse;
import com.cham.feedbacck.great.enums.GreatType;
import com.cham.feedbacck.great.service.GreatService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.*;

class GreatServiceImplTest extends RepositoryAndServiceTestSupport {
    
    @Autowired
    private GreatService greatService;
    
    @DisplayName("")
    @Test
    void test1() {
        
        GreatPostRequest request = GreatPostRequest
                .builder()
                .greatType(GreatType.OPPOSITION)
                .memberId(2L)
                .articleId(1L)
                .build();
        
        
        ApiResponse great = greatService.createGreat(request);
        System.out.println("great: " + great);
    }
    
    @DisplayName("")
    @Test
    void test2(){
        GreatResponse greats = greatService.getGreats(1L, 1L);
        System.out.println("greats: " + greats);
    }
}