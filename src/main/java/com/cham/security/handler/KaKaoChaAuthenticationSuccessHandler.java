package com.cham.security.handler;

import com.cham.entity.Member;
import com.cham.security.jwt.JwtTokenProvider;
import com.cham.security.token.KaKaoChamAuthenticationToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component(value = "kaKaoChamAuthenticationSuccessHandler")
@RequiredArgsConstructor
public class KaKaoChaAuthenticationSuccessHandler implements AuthenticationSuccessHandler {
    
    private final JwtTokenProvider jwtTokenProvider;
    private final ObjectMapper objectMapper;
    
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        KaKaoChamAuthenticationToken kakaoToken = (KaKaoChamAuthenticationToken) authentication;
        Member member = (Member) kakaoToken.getPrincipal();
        String profileImageUrl = kakaoToken.getProfileImageUrl();
        String thumbnailImageUrl = kakaoToken.getThumbnailImageUrl();
        String token = jwtTokenProvider.createToken(member.getMemberId(), member.getMemberEmail(), member.getRole().name(),   profileImageUrl, thumbnailImageUrl, member.getMemberName(),member.getMemberSubId());
        Map<String,Object> map = new HashMap<>();
        map.put("id",member.getMemberId());
        map.put("token",token);
        String jwtToken = objectMapper.writeValueAsString(map);
        response.getWriter().write(jwtToken);
    }
}
