package com.cham.service.impl;

import com.cham.controller.request.ReplyModifyRequest;
import com.cham.controller.request.ReplyCreateRequest;
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
    public ApiResponse insertReply(ReplyCreateRequest request) {
        Reply reply = new Reply(request.getMemberId(), request.getCardUseAddrId(), request.getReplyCont());
        replyRepository.save(reply);
        return new ApiResponse(200,true,"댓글이 작성 되었습니다.");
    }
    
    @Override
    public ApiResponse updateReply(ReplyModifyRequest request) {
        Long replyId = request.getReplyId();
        Reply reply = replyRepository.findById(replyId).orElseThrow(() -> new RuntimeException("존재하지 않는 댓글입니다."));
        reply.modifyReply(request);
        return new ApiResponse(200,true,"댓글이 수정 되었습니다.");
    }
    
    @Override
    public ApiResponse deleteReply(Long replyId) {
        Reply reply = replyRepository.findById(replyId).orElseThrow(() -> new RuntimeException("존재하지 않는 댓글입니다."));
        replyRepository.delete(reply);
        return new ApiResponse(200,true,"댓글이 삭제 되었습니다.");
    }
}
