package com.cham.security.provider;

import com.cham.security.context.ChamMonimapMemberContext;
import com.cham.security.service.KaKaoChamUserDetailService;
import com.cham.security.service.SocialService;
import com.cham.security.service.impl.response.AccessTokenResponse;
import com.cham.security.service.impl.response.KaKaoProfileResponse;
import com.cham.security.token.SocialAuthenticationToken;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SocialAuthenticationProvider implements AuthenticationProvider {
    
    private final SocialService kaKaoService;
    private final KaKaoChamUserDetailService chamUserDetailService;
    
    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        
        SocialAuthenticationToken token = (SocialAuthenticationToken) authentication;
        
        String authorizeCode = (String) token.getPrincipal();
        AccessTokenResponse accessToken = kaKaoService.getAccessToken(authorizeCode);
        KaKaoProfileResponse kaKaoProfile = kaKaoService.getKaKaoProfile(accessToken.getAccess_token());
        String profileImageUrl = kaKaoProfile.getKakaoAccount().getProfile().getProfileImageUrl();
        String thumbnailImageUrl = kaKaoProfile.getKakaoAccount().getProfile().getThumbnailImageUrl();
        ChamMonimapMemberContext userServiceContext = (ChamMonimapMemberContext) chamUserDetailService.loadUserByUsername(kaKaoProfile);
        
        
        return new SocialAuthenticationToken(userServiceContext.getMember(), null,profileImageUrl,thumbnailImageUrl,userServiceContext.getAuthorities());
    }
    
    
    @Override
    public boolean supports(Class<?> authentication) {
        return SocialAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
