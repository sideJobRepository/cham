package com.cham.feedbacck.legislation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
public class LegislationReplyOnlyResponse {
    
    private Long legislationId;
    private String title;
    private String billVersion;
    private Long replyCount;
    private List<Reply> replies;
    
    @Getter
    @AllArgsConstructor
    public static class Reply {
        private Long replyId;
        private Long memberId;
        private String memberName;
        private String content;
        private LocalDateTime registDate;
    }
}