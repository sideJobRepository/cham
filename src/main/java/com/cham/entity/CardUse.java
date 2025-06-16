package com.cham.entity;

import com.cham.entity.base.BaseData;
import jakarta.persistence.*;
import lombok.Getter;

import java.util.Date;


@Entity
@Getter
@Table(name = "CARD_USE")
public class CardUse extends BaseData {
    
    // 카드 사용 ID
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CARD_USE_ID")
    private Long cardUseId;
    
    // 카드 승인자 ID
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CARD_OWNER_ID")
    private CardOwner cardOwner;
    
    // 카드 사용 일시
    @Column(name = "CARD_USE_DATE")
    private Date cardUseDate;
    
    // 카드 사용 시간
    @Column(name = "CARD_USE_TIME")
    private Date cardUseTime;
    
    // 카드 사용 장소
    @Column(name = "CARD_USE_ADDR")
    private String cardUseAddr;
    
    // 카드 사용 목적
    @Column(name = "CARD_USE_PURPOSE")
    private String cardUsePurpose;
    
    // 카드 사용 인원
    @Column(name = "CARD_USE_PERSONNEL")
    private String cardUsePersonnel;
    
    // 카드 사용 금액
    @Column(name = "CARD_USE_AMOUNT")
    private Integer cardUseAmount;

}
