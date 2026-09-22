package com.cham.feedbacck.reply.controller;

import com.cham.dto.response.ApiResponse;
import com.cham.feedbacck.reply.dto.request.LegislationArticleReplyPostRequest;
import com.cham.feedbacck.reply.dto.request.LegislationArticleReplyPutRequest;
import com.cham.feedbacck.reply.dto.response.LegislationArticleReplyGetRequest;
import com.cham.feedbacck.reply.service.LegislationArticleReplyService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/cham")
@RequiredArgsConstructor
public class LegislationArticleReplyController {
    
    private final LegislationArticleReplyService legislationArticleReplyService;
    
    @GetMapping("/article-reply/{articleId}")
    public LegislationArticleReplyGetRequest getReplies(@PathVariable Long articleId, @AuthenticationPrincipal Jwt jwt) {
        Long memberId = Optional.ofNullable(jwt)
                .map(token -> token.getClaim("id"))
                .map(Object::toString)
                .map(Long::valueOf)
                .orElse(null);
        return legislationArticleReplyService.getReplies(articleId, memberId);
    }
    
    @PostMapping("/article-reply")
    public ApiResponse createReply(@RequestBody LegislationArticleReplyPostRequest request, @AuthenticationPrincipal Jwt jwt) {
        Long memberId = Optional.ofNullable(jwt)
                .map(token -> token.getClaim("id"))
                .map(Object::toString)
                .map(Long::valueOf)
                .orElse(null);
        return legislationArticleReplyService.createReply(request, memberId);
    }
    
    @PutMapping("/article-reply")
    public ApiResponse modifyReply(@RequestBody LegislationArticleReplyPutRequest request) {
        return legislationArticleReplyService.modifyReply(request);
    }
    
    @DeleteMapping("/article-reply/{replyId}")
    public ApiResponse deleteReply(@PathVariable Long replyId) {
        return legislationArticleReplyService.deleteReply(replyId);
    }
}
