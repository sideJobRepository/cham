package com.cham.security.service.impl;

import com.cham.security.service.SocialService;
import com.cham.security.service.impl.response.AccessTokenResponse;
import com.cham.security.service.impl.response.SocialProfile;
import com.enumtype.SocialType;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("googleService")
@Transactional
@RequiredArgsConstructor
public class GoogleServiceImpl implements SocialService {
    
    @Value("${google.clientId}")
    private String googleClientId;
    @Value("${google.redirecturi}")
    private String googleRedirectUri;
    @Value("${google.client-secret}")
    private String googleClientSecret;
    
    @Override
    public SocialType type() {
        return SocialType.GOOGLE;
    }
    
    @Override
    public AccessTokenResponse getAccessToken(String code) {
        return null;
    }
    
    @Override
    public SocialProfile getProfile(String accessToken) {
        return null;
    }
}
