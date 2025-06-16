package com.cham.entity;


import com.cham.entity.base.BaseData;
import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Getter
@Table(name = "CARD_OWNER")
public class CardOwner extends BaseData {
    
    // 카드 승인자 ID
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CARD_OWNER_ID")
    private Long cardOwnerId;
    
    // 카드 승인자 직위 ID
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CARD_OWNER_POSITION_ID")
    private CardOwnerPosition cardOwnerPosition;
    
    // 카드 승인자 이름
    @Column(name = "CARD_OWNER_NAME")
    private String cardOwnerName;
    
}
