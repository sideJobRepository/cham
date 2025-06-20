package com.cham.service.impl;

import com.cham.controller.request.ReplyRequest;
import com.cham.controller.response.ApiResponse;
import com.cham.entity.Reply;
import com.cham.repository.ReplyRepository;
import com.cham.service.ReplyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
@Transactional
public class ReplyServiceImpl implements ReplyService {
    
    private final ReplyRepository replyRepository;
    
    @Override
    public ApiResponse insertReply(ReplyRequest request) {
        Reply reply = new Reply(request.getMemberId(), request.getCardUseAddrId(), request.getReplyCont());
        replyRepository.save(reply);
        return new ApiResponse(200,true,"댓글이 작성 되었습니다.");
    }
}
