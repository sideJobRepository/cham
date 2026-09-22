package com.cham.cardowner.entity;

import com.cham.base.BaseData;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "CHAM_MONIMAP_CARD_OWNER_POSITION")
@NoArgsConstructor
public class ChamMonimapCardOwnerPosition extends BaseData {
    
    // 카드 승인자 직위 ID
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CHAM_MONIMAP_CARD_OWNER_POSITION_ID")
    private Long chamMonimapCardOwnerPositionId;
    
    // 카드 승인자 직위 이름
    @Column(name = "CHAM_MONIMAP_CARD_OWNER_POSITION_NAME")
    private String chamMonimapCardOwnerPositionName;
    
    public ChamMonimapCardOwnerPosition(Long chamMonimapCardOwnerPositionId) {
        this.chamMonimapCardOwnerPositionId = chamMonimapCardOwnerPositionId;
    }
    
    public ChamMonimapCardOwnerPosition(String chamMonimapCardOwnerPositionName) {
        this.chamMonimapCardOwnerPositionName = chamMonimapCardOwnerPositionName;
    }
}
