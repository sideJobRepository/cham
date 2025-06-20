package com.cham.controller.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReplyRequest {
    
    
    private Long cardUseAddrId;
    
    private Long memberId;
    
    private String replyCont;
    
}
