package com.cham.security.handler;

import com.cham.member.entity.ChamMonimapMember;
import com.cham.security.dto.ChamMonimapMemberResponseDto;
import com.cham.security.jwt.RsaSecuritySigner;
import com.cham.security.token.SocialAuthenticationToken;
import com.cham.refeshtoken.service.ChamMonimapRefreshTokenService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.JWK;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Component(value = "chamMonimapSuccessHandler")
@RequiredArgsConstructor
public class ChamMonimapSuccessHandler implements AuthenticationSuccessHandler {

    private final ObjectMapper objectMapper;
    private final RsaSecuritySigner rsaSecuritySigner;
    private final ChamMonimapRefreshTokenService chamMonimapRefreshTokenService;
    private final JWK jwk;
    @Value("${cookie.secure}")
    private boolean secure;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        SocialAuthenticationToken token = (SocialAuthenticationToken) authentication;
        ChamMonimapMember member = (ChamMonimapMember) token.getPrincipal();
        List<GrantedAuthority> authorities = (List<GrantedAuthority>) token.getAuthorities();

        LocalDateTime expiresAt = LocalDateTime.now().plusDays(1);

        try {
            TokenPair tokenPair = rsaSecuritySigner.getToken(member, jwk, authorities);
            chamMonimapRefreshTokenService.refreshTokenSaveOrUpdate(member, tokenPair.getRefreshToken(), expiresAt);

            ChamMonimapMemberResponseDto bgmAgitMemberResponseDto = ChamMonimapMemberResponseDto.create(member, authorities);
            // Access Token은 응답 JSON에 포함
            Map<String, Object> result = Map.of(
                    "user", bgmAgitMemberResponseDto,
                    "token", tokenPair.getAccessToken()
            );

            // Refresh Token은 HttpOnly 쿠키로 설정
            ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", tokenPair.getRefreshToken())
                    .httpOnly(true)
                    .secure(secure) // 로컬일 경우 secure=false
                    .path("/")
                    .maxAge(Duration.ofDays(1))
                    .sameSite("Strict")
                    .build();
            response.addHeader("Set-Cookie", refreshCookie.toString());
            response.setContentType("application/json; charset=UTF-8");
            response.getWriter().write(objectMapper.writeValueAsString(result));
        } catch (JOSEException e) {
            throw new RuntimeException(e);
        }
    }
}
