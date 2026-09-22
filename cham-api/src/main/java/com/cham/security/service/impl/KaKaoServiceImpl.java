package com.cham.security.service.impl;

import com.cham.security.service.SocialService;
import com.cham.security.service.impl.response.AccessTokenResponse;
import com.cham.security.service.impl.response.KakaoProfileResponse;
import com.cham.security.service.impl.response.SocialProfile;
import com.enumtype.SocialType;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

@Service("kakaoService")
@Transactional
@RequiredArgsConstructor
public class KaKaoServiceImpl implements SocialService {
    
    @Value("${kakao.clientId}")
    private String kakaoClientId;
    @Value("${kakao.redirecturi}")
    private String kakaoRedirectUri;
    @Value("${kakao.redirecturi2}")
    private String kakaoRedirectUri2;
    @Value("${kakao.client-secret}")
    private String kakaoClientSecret;
    
    @Override
    public SocialType type() {
        return SocialType.KAKAO;
    }
    
    @Override
    public AccessTokenResponse getAccessToken(String code, String loginUrl) {
        RestClient restClient = RestClient.create();
        
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        
        params.add("code", code);
        params.add("client_id", kakaoClientId);
        if("/cham/kakao-login".equals(loginUrl)){
            params.add("redirect_uri", kakaoRedirectUri);
        }else {
            params.add("redirect_uri", kakaoRedirectUri2);
        }
        params.add("grant_type", "authorization_code");
        params.add("client_secret",kakaoClientSecret);
        
        ResponseEntity<AccessTokenResponse> response = restClient.post()
                .uri("https://kauth.kakao.com/oauth/token")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .body(params)
                .retrieve()
                .toEntity(AccessTokenResponse.class);
        
        return response.getBody();
    }
    
    @Override
    public SocialProfile getProfile(String accessToken) {
        KakaoProfileResponse resp = RestClient.create().get()
                .uri("https://kapi.kakao.com/v2/user/me")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .retrieve()
                .body(KakaoProfileResponse.class);   //  body만 받기
        
        Long id = resp != null ? resp.getId() : null;
        
        KakaoProfileResponse.KakaoAccount acc =
                resp != null ? resp.getKakaoAccount() : null;
        KakaoProfileResponse.Profile p =
                acc != null ? acc.getProfile() : null;
        
        String nickname      = p   != null ? p.getNickname()         : null;
        String profileImage  = p   != null ? p.getProfileImageUrl()  : null;
        String thumbnail     = p   != null ? p.getThumbnailImageUrl(): null;
        String email         = acc != null ? acc.getEmail()          : null;
        String name          = acc != null ? acc.getName()           : null;
        String phone         = acc != null ? acc.getPhoneNumber()    : null;
        
        return new SocialProfile(
                SocialType.KAKAO,
                id != null ? String.valueOf(id) : null,
                email,
                name,
                nickname,
                phone,
                profileImage,
                thumbnail
        );
        
    }
}
