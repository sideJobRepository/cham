package com.cham.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
public class CardUseResponse {
    
    private String addrName; // 장소 이름
    private String cardUseRegion;
    private String cardUseUser;
    private int visits; // 방문 횟수
    private String visitMember; // 방문 사람
    private Integer totalSum;
    private String addrDetail;
    private String cardUseImageUrl;
    private Long cardUseAddrId;
    private LocalDate useDate;
    private String x;
    private String y;
    private String categoryName;
    private String color;
    private List<CardUseGroupedResponse> cardUseGroupedResponses;
    private List<ReplyResponse> replies;
    
    
    
    
}
