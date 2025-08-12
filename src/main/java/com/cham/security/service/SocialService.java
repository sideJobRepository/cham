package com.cham.security.service;

import com.cham.security.service.impl.response.AccessTokenResponse;
import com.cham.security.service.impl.response.KaKaoProfileResponse;

public interface SocialService {
    
    AccessTokenResponse getAccessToken(String code);
    
    KaKaoProfileResponse getKaKaoProfile(String accessToken);
}
