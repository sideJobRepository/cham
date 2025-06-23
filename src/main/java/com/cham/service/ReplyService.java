package com.cham.service;

import com.cham.controller.request.ReplyModifyRequest;
import com.cham.controller.request.ReplyCreateRequest;
import com.cham.controller.response.ApiResponse;

public interface ReplyService {
    
    
    ApiResponse insertReply(ReplyCreateRequest request);
    
    ApiResponse updateReply(ReplyModifyRequest request);
    
    ApiResponse deleteReply(Long request);
}
