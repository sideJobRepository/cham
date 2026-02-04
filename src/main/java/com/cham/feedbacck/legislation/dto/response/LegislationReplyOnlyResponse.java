package com.cham.feedbacck.legislation.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
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
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
        private LocalDateTime registDate;
    }
}