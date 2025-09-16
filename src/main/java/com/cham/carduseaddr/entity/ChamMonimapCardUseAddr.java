package com.cham.carduseaddr.entity;


import com.base.BaseData;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "CHAM_MONIMAP_CARD_USE_ADDR")
@NoArgsConstructor
public class ChamMonimapCardUseAddr extends BaseData {
    
    
    // 카드 사용 장소 ID
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CHAM_MONIMAP_CARD_USE_ADDR_ID")
    private Long chamMonimapCardUseAddrId;
    
    // 카드 사용 장소 이름
    @Column(name = "CHAM_MONIMAP_CARD_USE_ADDR_NAME")
    private String chamMonimapCardUseAddrName;
    
    // 카드 사용 상세 장소
    @Column(name = "CHAM_MONIMAP_CARD_USE_DETAIL_ADDR")
    private String chamMonimapCardUseDetailAddr;
    
    // 카드 사용 이미지 URL
    @Column(name = "CHAM_MONIMAP_CARD_USE_IMAGE_URL")
    private String chamMonimapCardUseImageUrl;
    
    public ChamMonimapCardUseAddr(String cardUseAddrNameValue, String cardUseDetailAddrValue) {
        this.chamMonimapCardUseAddrName = cardUseAddrNameValue;
        this.chamMonimapCardUseDetailAddr = cardUseDetailAddrValue;
    }
    
    public ChamMonimapCardUseAddr(Long cardUseAddrId) {
        this.chamMonimapCardUseAddrId = cardUseAddrId;
    }
    
    public void updateImage(String s3Url) {
        this.chamMonimapCardUseImageUrl = s3Url;
    }
}
