package com.cham.security.jwt;


import com.cham.repository.ChamMonimapRsaRepository;
import com.cham.security.pem.PemKey;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.RSAKey;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.InputStream;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

@Configuration
@RequiredArgsConstructor
public class SignatureConfig {
    
    
    private final ChamMonimapRsaRepository chamMonimapRsaRepository;
    
    
    @Bean
    public RsaSecuritySigner rsaSigner() {
        return new RsaSecuritySigner();
    }
    
    @Bean
    public RSAKey rsaKey() throws Exception {
        InputStream publicKeyStream = new ClassPathResource("keys/public.pem").getInputStream();
        RSAPublicKey publicKey = PemKey.loadPublicKey(publicKeyStream);
        RSAPrivateKey privateKey = PemKey.loadPrivateKey(chamMonimapRsaRepository.findAll().get(0).getChamMonimapPrivateKey());
        return new RSAKey.Builder(publicKey)
                .privateKey(privateKey)
                .keyID("rsaKey")
                .algorithm(JWSAlgorithm.RS256)
                .build();
    }
}
