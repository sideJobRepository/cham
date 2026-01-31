package com.cham.feedbacck.reply.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class LegislationArticleReplyGetRequest {
    
    private Long articleId;
    private List<Reply> replies;

    /* =========================
       중첩 DTO
       ========================= */
    
    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    @Builder
    public static class Reply {
        
        private Long replyId;        // 댓글 ID
        
        private Long memberId;       // 작성자 ID
        private String memberName;   // 작성자 이름
        
        private String content;      // 댓글 내용
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
        private LocalDateTime registDate;
        private Boolean isOwner;
        private List<Reply> children;
    }
}
