package com.cham.security.provider;

import com.cham.security.context.ChamMonimapMemberContext;
import com.cham.security.service.SocialService;
import com.cham.security.service.impl.ChamUserDetailService;
import com.cham.security.service.impl.response.AccessTokenResponse;
import com.cham.security.service.impl.response.SocialProfile;
import com.cham.security.token.SocialAuthenticationToken;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SocialAuthenticationProvider implements AuthenticationProvider {
    
    private final SocialService kakaoService;
    private final SocialService naverService;
    private final SocialService googleService;
    private final ChamUserDetailService chamUserDetailService;
    
    
    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        SocialAuthenticationToken token = (SocialAuthenticationToken) authentication;
   
       String loginUrl = token.getLoginUrl();
       String authorizeCode = (String) token.getPrincipal();
   
       // 해당 loginUrl에 맞는 소셜 서비스 가져오기
       SocialService socialService = getSocialServiceByLoginUrl(loginUrl);
       AccessTokenResponse accessToken = socialService.getAccessToken(authorizeCode);
       SocialProfile profile = socialService.getProfile(accessToken.getAccess_token());
   
       // 공통 처리
       String profileImageUrl = profile.profileImageUrl();
       String thumbnailImageUrl = profile.thumbnailImageUrl();
   
       ChamMonimapMemberContext context = (ChamMonimapMemberContext) chamUserDetailService.loadUserByUsername(profile);
   
       // 토큰 생성
       return new SocialAuthenticationToken(
               context.getMember(),
               null,
               profileImageUrl,
               thumbnailImageUrl,
               null,
               context.getAuthorities()
       );
    }
    
    
    @Override
    public boolean supports(Class<?> authentication) {
        return SocialAuthenticationToken.class.isAssignableFrom(authentication);
    }
    
    private SocialService getSocialServiceByLoginUrl(String loginUrl) {
        return switch (loginUrl) {
           case "/cham/kakao-login" -> kakaoService;
           case "/cham/naver-login" -> naverService;
           case "/cham/google-login" -> googleService;
           default -> throw new BadCredentialsException("존재하지 않는 소셜 로그인 URL입니다: " + loginUrl);
       };
    }
}
