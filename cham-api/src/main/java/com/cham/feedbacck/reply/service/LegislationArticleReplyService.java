package com.cham.feedbacck.reply.service;

import com.cham.dto.response.ApiResponse;
import com.cham.feedbacck.reply.dto.request.LegislationArticleReplyPostRequest;
import com.cham.feedbacck.reply.dto.request.LegislationArticleReplyPutRequest;
import com.cham.feedbacck.reply.dto.response.LegislationArticleReplyGetRequest;

public interface LegislationArticleReplyService {
    
    LegislationArticleReplyGetRequest getReplies(Long articleId,Long loginMemberId);
    ApiResponse createReply(LegislationArticleReplyPostRequest request,Long memberId);
    ApiResponse modifyReply(LegislationArticleReplyPutRequest request);
    ApiResponse deleteReply(Long replyId);
}
