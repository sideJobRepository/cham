package com.cham.service.impl;

import com.cham.controller.response.ApiResponse;
import com.cham.entity.Reply;
import com.cham.service.ReplyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
@Transactional
public class ReplyServiceImpl implements ReplyService {
    
    
    @Override
    public ApiResponse insertReply(Reply reply) {
        return null;
    }
}
