package com.cham.service.impl;

import com.cham.RepositoryAndServiceTestSupport;
import com.cham.controller.request.ReplyCreateRequest;
import com.cham.service.ReplyService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.*;

class ReplyServiceImplTest extends RepositoryAndServiceTestSupport {
    
    
    @Autowired
    private ReplyService replyService;
    
    
    @DisplayName("")
    @Test
    void test(){
        //given
        ReplyCreateRequest replyCreateRequest = new ReplyCreateRequest(5L, 1L, "테스트댓글");
        
        
        replyService.insertReply(replyCreateRequest);
        
    
    }
}