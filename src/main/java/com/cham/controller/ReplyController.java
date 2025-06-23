package com.cham.controller;


import com.cham.controller.request.ReplyDeleteRequest;
import com.cham.controller.request.ReplyModifyRequest;
import com.cham.controller.request.ReplyCreateRequest;
import com.cham.controller.response.ApiResponse;
import com.cham.service.ReplyService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cham")
@RequiredArgsConstructor
public class ReplyController {
    
    private final ReplyService replyService;
    
    @PostMapping("/reply")
    public ApiResponse createReply(@RequestBody ReplyCreateRequest request) {
        return replyService.insertReply(request);
    }
    
    @PutMapping("/reply")
    public ApiResponse updateReply(@RequestBody ReplyModifyRequest request) {
        return replyService.updateReply(request);
    }
    
    @DeleteMapping("/reply/{replyId}")
    public ApiResponse deleteReply(@PathVariable Long replyId) {
        return replyService.deleteReply(replyId);
    }


}
