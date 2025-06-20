package com.cham.service;

import com.cham.controller.request.ReplyRequest;
import com.cham.controller.response.ApiResponse;

public interface ReplyService {
    
    
    ApiResponse insertReply(ReplyRequest reply);
}
