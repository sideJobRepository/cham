package com.cham.security.jwt;


import com.cham.member.entity.ChamMonimapMember;
import com.cham.security.handler.TokenPair;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.RSAKey;
import org.springframework.security.core.GrantedAuthority;

import java.util.List;

public class RsaSecuritySigner extends SecuritySigner{
    @Override
    public TokenPair getToken(ChamMonimapMember user, JWK jwk, List<GrantedAuthority> authorities) throws JOSEException {
        JWSSigner signer = new RSASSASigner(((RSAKey) jwk).toRSAPrivateKey());
        
        String accessToken = super.generateAccessToken(signer, user, jwk, authorities);
        String refreshToken = super.generateRefreshToken(signer, user, jwk);
        
        return new TokenPair(accessToken, refreshToken);
    }
}
