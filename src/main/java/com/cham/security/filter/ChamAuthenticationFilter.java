package com.cham.security.filter;

import com.cham.security.service.impl.request.SocialAuthorizeRequest;
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
                new AntPathRequestMatcher("/cham/kakao-login", "POST"),
                new AntPathRequestMatcher("/cham/naver-login", "POST"),
                new AntPathRequestMatcher("/cham/google-login", "POST")
        ));
    }
    
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException, IOException {
        
        String url = request.getRequestURI();
        SocialAuthorizeRequest socialAuthorizeRequest = objectMapper.readValue(request.getInputStream(), SocialAuthorizeRequest.class);
        
        String code = socialAuthorizeRequest.getCode();
        
        SocialAuthenticationToken socialAuthenticationToken = new SocialAuthenticationToken(
                code,
                null,
                null,
                null,
                url
        );
        
        return this.getAuthenticationManager().authenticate(socialAuthenticationToken);
    }
}
