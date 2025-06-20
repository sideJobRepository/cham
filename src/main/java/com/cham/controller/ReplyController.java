package com.cham.controller;


import com.cham.controller.request.ReplyRequest;
import com.cham.controller.response.ApiResponse;
import com.cham.service.ReplyService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/cham")
@RequiredArgsConstructor
public class ReplyController {
    
    private final ReplyService replyService;
    
    @PostMapping("/reply")
    public ApiResponse createReply(@RequestBody ReplyRequest request) {
        replyService.insertReply(request);
        return null;
    }


}
