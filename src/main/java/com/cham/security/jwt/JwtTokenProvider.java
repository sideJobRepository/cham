package com.cham.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.Getter;
import org.springframework.stereotype.Component;

import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.util.Date;

@Component
@Getter
public class JwtTokenProvider {
    
    private final String secretKey = "cW90amRnaGtzeHB0bXhtcW90amRnaGtzeHB0bXhtcW90amRnaGtzeHB0bXhtcW90amRnaGtzeHB0bXht";
   
    private final int expiration = 3000;
    
    private final Key encryptionKey;
    
    public JwtTokenProvider() {
        this.encryptionKey = new SecretKeySpec(java.util.Base64.getDecoder().decode(secretKey), SignatureAlgorithm.HS512.getJcaName());
    }
    
    public String createToken(String email, String role, String profileImageUrl, String thumbnailImageUrl, String memberName) {
        // Claims 는 jwt 토큰의 payload 부분을 의미
        Claims claims = Jwts.claims().setSubject(email);
        claims.put("role", role);
        claims.put("profileImageUrl", profileImageUrl);
        claims.put("thumbnailImageUrl", thumbnailImageUrl);
        claims.put("memberName", memberName);
        Date now = new Date();
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + expiration * 60 * 1000L))
                .signWith(encryptionKey)
                .compact();
    }
}
