package com.cham.feedbacck.reply.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class LegislationArticleReplyPostRequest {
    
    // 조문 ID
    private Long articleId;
    
    private Long id;
    
    // 부모 댓글 ID (대댓글일 때만, 없으면 null)
    private Long parentReplyId;
    
    // 댓글 내용
    private String content;
    
}
