package com.cham.entity;

import com.cham.entity.base.BaseData;
import com.cham.entity.dto.CardOwnerPositionDto;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;


@Entity
@Getter
@Table(name = "CARD_USE")
@NoArgsConstructor
public class CardUse extends BaseData {
    
    // 카드 사용 ID
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CARD_USE_ID")
    private Long cardUseId;
    
    // 카드 승인자 ID
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CARD_OWNER_POSITION_ID")
    private CardOwnerPosition cardOwnerPosition;
    
    // 카드 사용 장소 ID
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CARD_USE_ADDR_ID")
    private CardUseAddr cardUseAddr;
    
    // 카드 사용 이름
    @Column(name = "CARD_USE_NAME")
    private String cardUseName;
    
    // 카드 사용 일시
    @Column(name = "CARD_USE_DATE")
    private LocalDate cardUseDate;
    
    // 카드 사용 시간
    @Column(name = "CARD_USE_TIME")
    private LocalTime cardUseTime;
    
    // 카드 사용 목적
    @Column(name = "CARD_USE_PURPOSE")
    private String cardUsePurpose;
    
    // 카드 사용 인원
    @Column(name = "CARD_USE_PERSONNEL")
    private String cardUsePersonnel;
    
    // 카드 사용 금액
    @Column(name = "CARD_USE_AMOUNT")
    private Integer cardUseAmount;
    
    // 카드 사용 방법
    @Column(name = "CARD_USE_METHOD")
    private String cardUseMethod;
    
    // 카드 사용 비고
    @Column(name = "CARD_USE_REMARK")
    private String cardUseRemark;
    
    
    public CardUse(List<CardOwnerPositionDto> cardOwnerPositionDtos, CardUseAddr cardUserAddr, String userSellValue, String nameSellValue, LocalDate dateValue, LocalTime timeValue, String purpose, String personnel, double amount, String method, String remark) {
        this(cardOwnerPositionDtos, userSellValue, nameSellValue);
        this.cardUseAddr = cardUserAddr;
        this.cardUseDate = dateValue;
        this.cardUseTime = timeValue;
        this.cardUsePurpose = purpose;
        this.cardUsePersonnel = personnel.replace("명", "").trim();
        this.cardUseAmount = (int) amount;
        this.cardUseMethod = method;
        this.cardUseRemark = remark;
    }
    
    public CardUse(List<CardOwnerPositionDto> cardOwnerPositionDtos,String userSell , String nameSell ) {
        this.cardUseName = nameSell;
        Long defaultId = null;
        for (CardOwnerPositionDto dto : cardOwnerPositionDtos) {
            if ("기타".equals(dto.getCardOwnerPositionName())) {
                defaultId = dto.getCardOwnerPositionId();
            }
            if (userSell.equals(dto.getCardOwnerPositionName())) {
                this.cardOwnerPosition = new CardOwnerPosition(dto.getCardOwnerPositionId());
                return;
            }
        }
        if(defaultId != null) {
            this.cardOwnerPosition = new CardOwnerPosition(defaultId);
        }
    }
}
