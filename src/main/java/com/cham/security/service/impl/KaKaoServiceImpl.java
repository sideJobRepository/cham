package com.cham.security.service.impl;

import com.cham.security.service.KaKaoService;
import com.cham.security.service.impl.response.AccessTokenResponse;
import com.cham.security.service.impl.response.KaKaoProfileResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

public class KaKaoServiceImpl implements KaKaoService {
    
    private final String kakaoClientId = ""; //값 넣어야함
    private final String kakaoRedirectUri = "http://localhost:5173/oauth/kakao/redirect"; // 바꿀예정
    
    @Override
    public AccessTokenResponse getAccessToken(String code) {
        RestClient restClient = RestClient.create();
        
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        
        params.add("code", code);
        params.add("client_id", kakaoClientId);
        params.add("redirect_uri", kakaoRedirectUri);
        params.add("grant_type", "authorization_code");
        
        ResponseEntity<AccessTokenResponse> response = restClient.post()
                .uri("https://kauth.kakao.com/oauth/token")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .body(params)
                .retrieve()
                .toEntity(AccessTokenResponse.class);
        
        return response.getBody();
    }
    
    @Override
    public KaKaoProfileResponse getKaKaoProfile(String accessToken) {
        RestClient restClient = RestClient.create();
        
        ResponseEntity<KaKaoProfileResponse> response = restClient.post()
                .uri("https://kapi.kakao.com/v2/user/me")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .toEntity(KaKaoProfileResponse.class);
        
        return response.getBody();
    }
}
