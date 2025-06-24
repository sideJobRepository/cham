package com.cham.controller.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ReplyResponse {
    
    private Long replyId;
    
    private String replyCont;
    
    private String memberName;
    
    private String memberImageUrl;
    
    private String memberEmail;
    
    private List<String> replyImageUrls;
}
