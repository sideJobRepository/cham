package com.cham.caruse.entity;

import com.cham.cardowner.entity.ChamMonimapCardOwnerPosition;
import com.cham.carduseaddr.entity.ChamMonimapCardUseAddr;
import com.cham.base.BaseData;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.security.Security;
import java.time.LocalDate;
import java.time.LocalTime;


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

    // 지도 공개 여부. 삭제키(엑셀 업로드 한 건) 단위로 관리자가 켜고 끈다.
    // 행마다 들고 있지만 값을 바꾸는 것은 언제나 삭제키로 묶은 벌크 UPDATE 다.
    @Column(name = "CHAM_MONIMAP_CARD_USE_PUBLIC")
    private Boolean chamMonimapCardUsePublic;


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
        // 컬럼이 NOT NULL 이라 여기서 채워야 한다. 업로드 직후는 공개가 기본이다.
        this.chamMonimapCardUsePublic = Boolean.TRUE;
    }
    
    public String getAmountPerPerson() {
        if (this.chamMonimapCardUsePersonnel == null || chamMonimapCardUseAmount == null) {
            return null;
        }
    
        if (this.chamMonimapCardUsePersonnel.startsWith("내방객")) {
            return "내방객등";
        }
    
        // 💡 숫자만 남기기 (쉼표, 공백, 특수문자 제거)
        String personnelStr = this.chamMonimapCardUsePersonnel.replaceAll("[^0-9]", "");
    
        int personnel;
        try {
            personnel = Integer.parseInt(personnelStr);
        } catch (NumberFormatException e) {
            return null; // 예외 발생 시 그냥 null 반환
        }
    
        if (personnel == 0) return null;
    
        return String.format("%,d원", chamMonimapCardUseAmount / personnel); // 콤마 포함한 원 단위 출력
        
    }
}
