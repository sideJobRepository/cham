package com.cham.security.service;

import com.cham.security.service.impl.response.AccessTokenResponse;
import com.cham.security.service.impl.response.SocialProfile;
import com.enumtype.SocialType;

public interface SocialService {
    SocialType type();
    AccessTokenResponse getAccessToken(String code, String loginUrl);
    
    SocialProfile getProfile(String accessToken);
}
