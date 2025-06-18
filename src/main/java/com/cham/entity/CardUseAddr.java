package com.cham.entity;


import com.cham.entity.base.BaseData;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "CARD_USE_ADDR")
@NoArgsConstructor
public class CardUseAddr extends BaseData {
    
    
    // 카드 사용 장소 ID
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CARD_USE_ADDR_ID")
    private Long cardUseAddrId;
    
    // 카드 사용 장소 이름
    @Column(name = "CARD_USE_ADDR_NAME")
    private String cardUseAddrName;
    
    // 카드 사용 상세 장소
    @Column(name = "CARD_USE_DETAIL_ADDR")
    private String cardUseDetailAddr;
    
    // 카드 사용 이미지 URL
    @Column(name = "CARD_USE_IMAGE_URL")
    private String cardUseImageUrl;
    
    public CardUseAddr(String cardUseAddrNameValue, String cardUseDetailAddrValue) {
        this.cardUseAddrName = cardUseAddrNameValue;
        this.cardUseDetailAddr = cardUseDetailAddrValue;
    }
    
    public CardUseAddr(Long cardUseAddrId) {
        this.cardUseAddrId = cardUseAddrId;
    }
    
    public void updateImage(String s3Url) {
        this.cardUseImageUrl = s3Url;
    }
}
