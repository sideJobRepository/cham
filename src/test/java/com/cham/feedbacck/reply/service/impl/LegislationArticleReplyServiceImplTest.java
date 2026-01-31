package com.cham.feedbacck.reply.service.impl;


import com.cham.RepositoryAndServiceTestSupport;
import com.cham.dto.response.ApiResponse;
import com.cham.feedbacck.reply.dto.request.LegislationArticleReplyPostRequest;
import com.cham.feedbacck.reply.dto.request.LegislationArticleReplyPutRequest;
import com.cham.feedbacck.reply.dto.response.LegislationArticleReplyGetRequest;
import com.cham.feedbacck.reply.service.LegislationArticleReplyService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class LegislationArticleReplyServiceImplTest extends RepositoryAndServiceTestSupport {
    
    @Autowired
    private LegislationArticleReplyService legislationArticleReplyService;
    
    @DisplayName("")
    @Test
    void test1() {
        
        LegislationArticleReplyPostRequest result = LegislationArticleReplyPostRequest
                .builder()
                .articleId(1L)
                .parentReplyId(1L)
                .content("대댓글 내용1")
                .build();
        
        ApiResponse reply = legislationArticleReplyService.createReply(result, 2L);
        System.out.println("reply = " + reply);
        
    }
    
    @DisplayName("")
    @Test
    void test2(){
        LegislationArticleReplyGetRequest replies = legislationArticleReplyService.getReplies(1L, 2L);
        System.out.println("reply = " + replies);
    }
    
    @DisplayName("")
    @Test
    void test3(){
        ApiResponse apiResponse = legislationArticleReplyService.deleteReply(2L);
        System.out.println("apiResponse = " + apiResponse);
    }
    @DisplayName("")
    @Test
    void test4(){
        LegislationArticleReplyPutRequest request = LegislationArticleReplyPutRequest
                .builder()
                .replyId(3L)
                .content("댓글 asdsdsd 테스트")
                .build();
        ApiResponse apiResponse = legislationArticleReplyService.modifyReply(request);
        System.out.println("apiResponse = " + apiResponse);
    }
}