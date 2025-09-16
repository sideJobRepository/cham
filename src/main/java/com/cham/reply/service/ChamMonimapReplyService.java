package com.cham.reply.service;

import com.cham.dto.request.ReplyCreateRequest;
import com.cham.dto.request.ReplyModifyRequest;
import com.cham.dto.response.ApiResponse;

public interface ChamMonimapReplyService {
    
    
    ApiResponse insertReply(ReplyCreateRequest request);
    
    ApiResponse updateReply(ReplyModifyRequest request);
    
    ApiResponse deleteReply(Long request);
}
