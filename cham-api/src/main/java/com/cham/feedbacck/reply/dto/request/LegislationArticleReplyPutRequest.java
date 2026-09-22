package com.cham.feedbacck.reply.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class LegislationArticleReplyPutRequest {
    
    private Long replyId;
    
    private String content;
}
