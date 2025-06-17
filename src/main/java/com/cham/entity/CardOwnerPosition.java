package com.cham.entity;

import com.cham.entity.base.BaseData;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "CARD_OWNER_POSITION")
@NoArgsConstructor
public class CardOwnerPosition extends BaseData {
    
    // 카드 승인자 직위 ID
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CARD_OWNER_POSITION_ID")
    private Long cardOwnerPositionId;
    
    // 카드 승인자 직위 이름
    @Column(name = "CARD_OWNER_POSITION_NAME")
    private String cardOwnerPositionName;
    
    public CardOwnerPosition(Long cardOwnerPositionId) {
        this.cardOwnerPositionId = cardOwnerPositionId;
    }
}
