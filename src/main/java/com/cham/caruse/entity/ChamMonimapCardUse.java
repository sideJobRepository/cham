package com.cham.caruse.entity;

import com.cham.cardowner.entity.ChamMonimapCardOwnerPosition;
import com.cham.carduseaddr.entity.ChamMonimapCardUseAddr;
import com.base.BaseData;
import com.cham.dto.response.CardOwnerPositionDto;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;


@Entity
@Getter
@Table(name = "CHAM_MONIMAP_CARD_USE")
@NoArgsConstructor
public class ChamMonimapCardUse extends BaseData {
    
    // 카드 사용 ID
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CHAM_MONIMAP_CARD_USE_ID")
    private Long chamMonimapCardUseId;
    
    // 카드 승인자 ID
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CHAM_MONIMAP_CARD_OWNER_POSITION_ID")
    private ChamMonimapCardOwnerPosition chamMonimapCardOwnerPosition;
    
    // 카드 사용 장소 ID
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CHAM_MONIMAP_CARD_USE_ADDR_ID")
    private ChamMonimapCardUseAddr cardUseAddr;
    
    // 자치연대 예산감시 카드 사용 유저
    @Column(name = "CHAM_MONIMAP_CARD_USE_USER")
    private String chamMonimapCardUseUser;
    
    // 자치연대 예산감시 카드 사용 지역
    @Column(name = "CHAM_MONIMAP_CARD_USE_REGION")
    private String chamMonimapCardUseRegion;
    
    // 자치연대 예산감시 카드 사용 이름
    @Column(name = "CHAM_MONIMAP_CARD_USE_NAME")
    private String chamMonimapCardUseName;
    
    // 자치연대 예산감시 카드 사용 일시
    @Column(name = "CHAM_MONIMAP_CARD_USE_DATE")
    private LocalDate chamMonimapCardUseDate;
    
    // 자치연대 예산감시 카드 사용 시간
    @Column(name = "CHAM_MONIMAP_CARD_USE_TIME")
    private LocalTime chamMonimapCardUseTime;
    
    // 자치연대 예산감시 카드 사용 목적
    @Column(name = "CHAM_MONIMAP_CARD_USE_PURPOSE")
    private String chamMonimapCardUsePurpose;
    
    // 자치연대 예산감시 카드 사용 인원
    @Column(name = "CHAM_MONIMAP_CARD_USE_PERSONNEL")
    private String chamMonimapCardUsePersonnel;
    
    // 자치연대 예산감시 카드 사용 금액
    @Column(name = "CHAM_MONIMAP_CARD_USE_AMOUNT")
    private Integer chamMonimapCardUseAmount;
    
    // 자치연대 예산감시 카드 사용 방법
    @Column(name = "CHAM_MONIMAP_CARD_USE_METHOD")
    private String chamMonimapCardUseMethod;
    
    // 자치연대 예산감시 카드 사용 비고
    @Column(name = "CHAM_MONIMAP_CARD_USE_REMARK")
    private String chamMonimapCardUseRemark;
    
    // 자치연대 예산감시 카드 사용 삭제키
    @Column(name = "CHAM_MONIMAP_CARD_USE_DELKEY")
    private String chamMonimapCardUseDelkey;
    
    
    public ChamMonimapCardUse(ChamMonimapCardOwnerPosition cardOwnerPosition, ChamMonimapCardUseAddr cardUserAddr, String userSellValue, String nameSellValue,
                              LocalDate dateValue, LocalTime timeValue, String purpose, String personnel,
                              double amount, String method, String remark, String delKeyValue, String regionValue) {
        
        this.chamMonimapCardOwnerPosition = cardOwnerPosition;
        this.cardUseAddr = cardUserAddr;
        this.chamMonimapCardUseName = nameSellValue;
        this.chamMonimapCardUseUser = userSellValue;
        this.chamMonimapCardUseDate = dateValue;
        this.chamMonimapCardUseTime = timeValue;
        this.chamMonimapCardUsePurpose = purpose;
        this.chamMonimapCardUsePersonnel = personnel.replace("명", "").trim();
        this.chamMonimapCardUseAmount = (int) amount;
        this.chamMonimapCardUseMethod = method;
        this.chamMonimapCardUseRemark = remark;
        this.chamMonimapCardUseDelkey = delKeyValue;
        this.chamMonimapCardUseRegion = regionValue;
    }
    
    public ChamMonimapCardUse(List<CardOwnerPositionDto> cardOwnerPositionDtos, String userSell , String nameSell ) {
        this.chamMonimapCardUseName = nameSell;
        Long defaultId = null;
        for (CardOwnerPositionDto dto : cardOwnerPositionDtos) {
            if ("기타".equals(dto.getCardOwnerPositionName())) {
                defaultId = dto.getCardOwnerPositionId();
            }
            if (userSell.equals(dto.getCardOwnerPositionName())) {
                this.chamMonimapCardOwnerPosition = new ChamMonimapCardOwnerPosition(dto.getCardOwnerPositionId());
                return;
            }
        }
        if(defaultId != null) {
            this.chamMonimapCardOwnerPosition = new ChamMonimapCardOwnerPosition(defaultId);
        }
    }
    
    public String getAmountPerPerson() {
        if (this.chamMonimapCardUsePersonnel == null ||  chamMonimapCardUseAmount == null && this.chamMonimapCardUseMethod == null) {
            return null; // 혹은 0 또는 예외 처리
        }
        if(this.chamMonimapCardUsePersonnel.startsWith("내방객")) {
            return "내방객등";
        }
        int personnel = Integer.parseInt(this.chamMonimapCardUsePersonnel);
        if (personnel == 0) return null; // 0명일 경우 예외 처리
        
        return String.format("%,d원", chamMonimapCardUseAmount / personnel); // 콤마 포함한 원 단위 출력
        
    }
}
