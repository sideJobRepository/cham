package com.cham.service;

import com.cham.controller.response.ApiResponse;
import com.cham.entity.Reply;

public interface ReplyService {
    
    
    ApiResponse insertReply(Reply reply);
}
