package com.cham.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.Getter;
import org.springframework.stereotype.Component;

import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.text.SimpleDateFormat;
import java.util.Date;

@Component
@Getter
public class JwtTokenProvider {
    
    private final String secretKey = "cW90amRnaGtzeHB0bXhtcW90amRnaGtzeHB0bXhtcW90amRnaGtzeHB0bXhtcW90amRnaGtzeHB0bXht";
    
    private final int expiration = 24 * 60; // 단위: 분 (24시간 = 1440분)
    
    private final Key encryptionKey;
    
    public JwtTokenProvider() {
        this.encryptionKey = new SecretKeySpec(java.util.Base64.getDecoder().decode(secretKey), SignatureAlgorithm.HS512.getJcaName());
    }
    
    public String createToken(Long memberId, String email, String role, String profileImageUrl, String thumbnailImageUrl, String memberName, String memberSubId) {
        // Claims 는 jwt 토큰의 payload 부분을 의미
        Date now = new Date();
        long expireMillis = now.getTime() + expiration * 60 * 1000L;
        Date expireDate = new Date(expireMillis);
        
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String expiredAtFormatted = format.format(expireDate);
        
        Claims claims = Jwts.claims().setSubject(memberSubId);
        claims.put("memberId", memberId);
        claims.put("email", email);
        claims.put("role", role);
        claims.put("profileImageUrl", profileImageUrl);
        claims.put("thumbnailImageUrl", thumbnailImageUrl);
        claims.put("memberName", memberName);
        claims.put("expiredAt", expiredAtFormatted); // 사용자용
        
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(expireDate) // JWT의 실제 만료 시간과 일치
                .signWith(encryptionKey)
                .compact();
    }
    
    /**
     * 테스트용 1초 인증 jwt
     * @param email
     * @param role
     * @param profileImageUrl
     * @param thumbnailImageUrl
     * @param memberName
     * @return
     */
    public String createTokenForTest(String email, String role, String profileImageUrl, String thumbnailImageUrl, String memberName) {
        Date now = new Date();
        long expireMillis = now.getTime() + 1000L; // 1초
        Date expireDate = new Date(expireMillis);
        
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String expiredAtFormatted = format.format(expireDate);
        
        Claims claims = Jwts.claims().setSubject(email);
        claims.put("role", role);
        claims.put("profileImageUrl", profileImageUrl);
        claims.put("thumbnailImageUrl", thumbnailImageUrl);
        claims.put("memberName", memberName);
        claims.put("expiredAt", expiredAtFormatted);
        
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(expireDate)
                .signWith(encryptionKey)
                .compact();
    }
}
