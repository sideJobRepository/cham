package com.cham.controller.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class CardUseResponse {
    
    private String addrName; // 장소 이름
    private int visits; // 방문 횟수
    private String visitMember; // 방문 사람
    private Integer totalSum;
    private String addrDetail;
    private List<CardUseGroupedResponse> cardUseGroupedResponses;
    
    
    
}
