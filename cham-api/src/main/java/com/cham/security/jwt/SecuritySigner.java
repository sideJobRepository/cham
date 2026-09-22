package com.cham.security.jwt;


import com.cham.member.entity.ChamMonimapMember;
import com.cham.security.handler.TokenPair;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.security.core.GrantedAuthority;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public abstract class SecuritySigner {
   
    
    protected String generateAccessToken(JWSSigner signer, ChamMonimapMember user, JWK jwk, List<GrantedAuthority> authorities) throws JOSEException {
        JWSHeader header = new JWSHeader.Builder((JWSAlgorithm) jwk.getAlgorithm())
                .keyID(jwk.getKeyID())
                .build();
        
        List<String> roleList = new ArrayList<>();
        if (authorities != null && !authorities.isEmpty()) {
            for (GrantedAuthority auth : authorities) {
                roleList.add("ROLE_" + auth.getAuthority());
            }
        }
        Date date = new Date(System.currentTimeMillis());
        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .subject("user")
                .claim("id", user.getChamMonimapMemberId())
                .claim("name", user.getChamMonimapMemberName())
                .claim("socialId", user.getChamMonimapMemberSubId())
                .claim("expirationTime",date)
                .claim("roles", roleList)
                .expirationTime(new Date(System.currentTimeMillis() + 1000 * 60 * 60)) // 1시간
                // .expirationTime(new Date(System.currentTimeMillis() + 1000 * 60 * 5)) // 5분
              //  .expirationTime(new Date(System.currentTimeMillis() +10))
                .build();
        
        SignedJWT jwt = new SignedJWT(header, claims);
        jwt.sign(signer);
        
        return jwt.serialize();
    }
    protected String generateRefreshToken(JWSSigner signer, ChamMonimapMember user, JWK jwk) throws JOSEException {
        JWSHeader header = new JWSHeader.Builder((JWSAlgorithm) jwk.getAlgorithm())
                .keyID(jwk.getKeyID())
                .build();
        
        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .subject("refresh")
                .claim("id", user.getChamMonimapMemberSubId())
                .expirationTime(new Date(System.currentTimeMillis() + 1000L * 60 * 60 * 24 * 7)) // 7일
                .build();
        
        SignedJWT jwt = new SignedJWT(header, claims);
        jwt.sign(signer);
        
        return jwt.serialize();
    }
    
    public abstract TokenPair getToken(ChamMonimapMember user, JWK jwk, List<GrantedAuthority> authorities) throws JOSEException;
}
