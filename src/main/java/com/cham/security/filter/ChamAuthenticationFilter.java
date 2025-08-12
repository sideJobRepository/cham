package com.cham.security.filter;

import com.cham.security.service.impl.request.KaKaoAuthorizeRequest;
import com.cham.security.token.SocialAuthenticationToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;

import java.io.IOException;

public class ChamAuthenticationFilter extends AbstractAuthenticationProcessingFilter {
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    public ChamAuthenticationFilter() {
        super(new OrRequestMatcher(
                new AntPathRequestMatcher("/cham/kakao-login", "POST"))
        );
    }
    
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException, IOException, ServletException {
        
        
        KaKaoAuthorizeRequest kaKaoAuthorizeRequest = objectMapper.readValue(request.getInputStream(), KaKaoAuthorizeRequest.class);
        
        String code = kaKaoAuthorizeRequest.getCode();
        
        SocialAuthenticationToken kaKaoChamAuthenticationToken = new SocialAuthenticationToken(
                code,
                null,
                null,
                null
        );
        
        return this.getAuthenticationManager().authenticate(kaKaoChamAuthenticationToken);
    }
}
