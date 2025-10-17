package com.cham.caruse.repository.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CardUseSummaryDto {
    private Long id;
    private String name;
    private Integer totalAmount;
}