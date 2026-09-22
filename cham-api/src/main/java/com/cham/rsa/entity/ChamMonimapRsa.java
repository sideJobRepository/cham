package com.cham.rsa.entity;


import com.cham.base.BaseData;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "CHAM_MONIMAP_RSA")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChamMonimapRsa extends BaseData {
    
    // 자치연대 예산감시 RSA ID
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CHAM_MONIMAP_RSA_ID")
    private Long chamMonimapRsaId;
    
    // 자치연대 예산감시 개인 키
    @Column(name = "CHAM_MONIMAP_PRIVATE_KEY")
    private String chamMonimapPrivateKey;
    
}
