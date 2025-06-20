package com.cham.controller.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ReplyModifyRequest {
    
    private Long replyId;
    
    private String replyCont;
}
