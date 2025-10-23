package com.cham.security.provider;

import com.cham.security.context.ChamMonimapMemberContext;
import com.cham.security.service.SocialService;
import com.cham.security.service.impl.ChamUserDetailService;
import com.cham.security.service.impl.response.AccessTokenResponse;
import com.cham.security.service.impl.response.KakaoProfileResponse;
import com.cham.security.service.impl.response.SocialProfile;
import com.cham.security.token.SocialAuthenticationToken;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
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
        if("/cham/kakao-login".equals(loginUrl)) {
            AccessTokenResponse accessToken = kakaoService.getAccessToken(authorizeCode);
            SocialProfile kaKaoProfile = kakaoService.getProfile(accessToken.getAccess_token());
            String profileImageUrl = kaKaoProfile.profileImageUrl();
            String thumbnailImageUrl = kaKaoProfile.thumbnailImageUrl();
            ChamMonimapMemberContext userServiceContext = (ChamMonimapMemberContext) chamUserDetailService.loadUserByUsername(kaKaoProfile);
            return new SocialAuthenticationToken(userServiceContext.getMember(), null,profileImageUrl,thumbnailImageUrl,null,userServiceContext.getAuthorities());
        } else if ("/cham/naver-login".equals(loginUrl)) {
            AccessTokenResponse accessToken = naverService.getAccessToken(authorizeCode);
            SocialProfile naverProfile = naverService.getProfile(accessToken.getAccess_token());
            String profileImageUrl = naverProfile.profileImageUrl();
            ChamMonimapMemberContext userServiceContext =(ChamMonimapMemberContext) chamUserDetailService.loadUserByUsername(naverProfile);
            return new SocialAuthenticationToken(userServiceContext.getMember(), null,profileImageUrl,null,null,userServiceContext.getAuthorities());
        }else if("/cham/google-login".equals(loginUrl)) {
            AccessTokenResponse accessToken = googleService.getAccessToken(authorizeCode);
            SocialProfile googleProfile = googleService.getProfile(accessToken.getAccess_token());
            String profileImageUrl = googleProfile.profileImageUrl();
            String thumbnailImageUrl = googleProfile.thumbnailImageUrl();;
            ChamMonimapMemberContext userServiceContext = (ChamMonimapMemberContext) chamUserDetailService.loadUserByUsername(googleProfile);
            return new SocialAuthenticationToken(userServiceContext.getMember(), null,profileImageUrl,thumbnailImageUrl,null,userServiceContext.getAuthorities());
        }
        
        throw new BadCredentialsException("존재 하지 않는 소셜 로그인 url 입니다. " + loginUrl);
    }
    
    
    @Override
    public boolean supports(Class<?> authentication) {
        return SocialAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
