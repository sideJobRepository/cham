package com.cham.reply.controller;


import com.cham.dto.request.ReplyCreateRequest;
import com.cham.dto.request.ReplyModifyRequest;
import com.cham.dto.response.ApiResponse;
import com.cham.reply.service.ChamMonimapReplyService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cham")
@RequiredArgsConstructor
public class ChamMonimapReplyController {
    
    private final ChamMonimapReplyService replyService;
    
    @PostMapping("/reply")
    public ApiResponse createReply(@ModelAttribute ReplyCreateRequest request) {
        return replyService.insertReply(request);
    }
    
    @PutMapping("/reply")
    public ApiResponse updateReply(@ModelAttribute ReplyModifyRequest request) {
        return replyService.updateReply(request);
    }
    
    @DeleteMapping("/reply/{replyId}")
    public ApiResponse deleteReply(@PathVariable Long replyId) {
        return replyService.deleteReply(replyId);
    }


}
