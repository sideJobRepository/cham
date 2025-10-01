package com.cham.dto.request;

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
    private String addrDetail;
    private LocalDate startDate;
    private LocalDate endDate;
    private String addrName;
    private Integer sortOrder; // 1: ASC, 2: DESC
}
