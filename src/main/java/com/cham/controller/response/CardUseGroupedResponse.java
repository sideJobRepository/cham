package com.cham.controller.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class CardUseGroupedResponse {

    private String userName;     // 사용자 이름
    private String amountPerPerson;
    private String cardUseMethod; // 카드 사용방법 (계좌 ,카드 등)
    private LocalDate useDate;   // 사용 일자
    private LocalTime useTime;   // 사용 시간
}
