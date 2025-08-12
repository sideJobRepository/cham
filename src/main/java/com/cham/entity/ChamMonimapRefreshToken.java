package com.cham.entity;

import com.cham.entity.base.BaseData;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "CHAM_MONIMAP_REFRESH_TOKEN")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ChamMonimapRefreshToken extends BaseData {
    
    // 자치연대 예산감시 리프레쉬 토큰 ID
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CHAM_MONIMAP_REFRESH_TOKEN_ID")
    private Long chamMonimapRefreshTokenId;
    
    // 자치연대 예산감시 회원 ID
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CHAM_MONIMAP_MEMBER_ID")
    private ChamMonimapMember chamMonimapMember;
    
    // 자치연대 예산감시 리프레쉬 토큰 값
    @Column(name = "CHAM_MONIMAP_REFRESH_TOKEN_VALUE")
    private String chamMonimapRefreshTokenValue;
    
    // 자치연대 예산감시 토큰 리프레쉬 만료 일시
    @Column(name = "CHAM_MONIMAP_TOKEN_REFRESH_EXPIRES_DATE")
    private LocalDateTime chamMonimapTokenRefreshExpiresDate;
    
    // 자치연대 예산감시 리프레쉬 플랫폼 ID
    @Column(name = "CHAM_MONIMAP_REFRESH_PLATFORM_ID")
    private String chamMonimapRefreshPlatformId;
    
    public void updateToken(String refreshTokenValue, LocalDateTime expiresAt) {
        this.chamMonimapRefreshTokenValue = refreshTokenValue;
        this.chamMonimapTokenRefreshExpiresDate = expiresAt;
    }
}
