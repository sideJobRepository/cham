package com.cham.security.provider;

import com.cham.security.service.KaKaoChamUserDetailService;
import com.cham.security.service.KaKaoService;
import com.cham.security.service.impl.response.AccessTokenResponse;
import com.cham.security.service.impl.response.KaKaoProfileResponse;
import com.cham.security.token.KaKaoChamAuthenticationToken;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KaKaoChamProvider implements AuthenticationProvider {
    
    private final KaKaoService kaKaoService;
    private final KaKaoChamUserDetailService chamUserDetailService;
    
    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        
        KaKaoChamAuthenticationToken token = (KaKaoChamAuthenticationToken) authentication;
        
        String authorizeCode = (String) token.getPrincipal();
        AccessTokenResponse accessToken = kaKaoService.getAccessToken(authorizeCode);
        KaKaoProfileResponse kaKaoProfile = kaKaoService.getKaKaoProfile(accessToken.getAccess_token());
        
        Long id = kaKaoProfile.getId();
        chamUserDetailService.loadUserByUsername(kaKaoProfile);
        
        return null;
    }
    
    
    @Override
    public boolean supports(Class<?> authentication) {
        return KaKaoChamAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
