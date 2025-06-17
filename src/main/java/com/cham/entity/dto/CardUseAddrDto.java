package com.cham.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class CardUseAddrDto {
    
    // 카드 사용 장소 ID
    private Long cardUseAddrId;
    
    // 카드 사용 장소 이름
    private String cardUseAddrName;
    
    // 카드 사용 상세 장소
    private String cardUseDetailAddr;
}
