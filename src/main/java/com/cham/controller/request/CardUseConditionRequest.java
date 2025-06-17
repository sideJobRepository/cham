package com.cham.controller.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CardUseConditionRequest {
    private Long cardOwnerPositionId;
    private String cardUseName;
    private Integer numberOfVisits;
    private LocalDate startDate;
    private LocalDate endDate;
}
