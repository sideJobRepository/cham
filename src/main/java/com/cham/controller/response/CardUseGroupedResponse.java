package com.cham.controller.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class CardUseGroupedResponse {

    private String userName;     // 사용자 이름
    private String amountPerPerson;
    private String cardUseMethod; // 카드 사용방법 (계좌 ,카드 등)
    private Integer cardUseAmount;
    private String cardUsePurpose;
    private String cardUsePersonnel;
    private String cardUseDate;
    
    public CardUseGroupedResponse(String userName, String amountPerPerson, String cardUseMethod, Integer cardUseAmount, String cardUsePurpose, String cardUsePersonnel, LocalDate useDate, LocalTime useTime) {
        this.userName = userName;
        this.amountPerPerson = amountPerPerson;
        this.cardUseMethod = cardUseMethod;
        this.cardUseAmount = cardUseAmount;
        this.cardUsePurpose = cardUsePurpose;
        this.cardUsePersonnel = cardUsePersonnel;
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        String useDateStr = useDate.format(dateFormatter);  // "2025-06-24"
        String useTimeStr = useTime.format(timeFormatter);  // "14:30"
        this.cardUseDate = useDateStr +  " " + useTimeStr;
    }
}
