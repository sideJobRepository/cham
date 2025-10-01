package com.cham.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CardUseConditionRequest {
    
    private Long cardOwnerPositionId; // 직위
    private String input; // 기관 / 지역 /사용자 / 이름 / 집행목적
}
