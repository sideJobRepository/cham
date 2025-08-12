package com.cham.controller;

import com.cham.controller.response.ApiResponse;
import com.cham.security.dto.TokenAndUser;
import com.cham.service.ChamMonimapRefreshTokenService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/cham")
public class ChamMonimapRefreshTokenController {
    
    private final ChamMonimapRefreshTokenService chamMonimapRefreshTokenService;
    
    @Value("${cookie.secure}")
    private boolean secure;
    
    @PostMapping("/refresh")
    public Map<String, Object> refreshToken(@CookieValue(value = "chamRefreshToken", required = false) String refreshToken
            , HttpServletResponse response
    ) {
        if(refreshToken == null) {
            return null;
        }
        TokenAndUser tokenPair = chamMonimapRefreshTokenService.reissueTokenWithUser(refreshToken);
        ResponseCookie newRefreshCookie = ResponseCookie.from("chamRefreshToken", tokenPair.token().getRefreshToken())
                .httpOnly(true)
                .secure(secure)
                .path("/")
                .maxAge(Duration.ofDays(1))
                .sameSite("Strict")
                .build();
        response.addHeader("Set-Cookie", newRefreshCookie.toString());
        return Map.of(
                "token", tokenPair.token().getAccessToken(),
                "user", tokenPair.user()
        );
    }
    
    @DeleteMapping("/refresh")
    public ApiResponse deleteRefreshToken(@CookieValue(value = "chamRefreshToken", required = false) String refreshToken
            , HttpServletResponse response) {
        if (refreshToken == null) {
            return null;
        }
        ApiResponse apiResponse = chamMonimapRefreshTokenService.deleteRefresh(refreshToken);
        ResponseCookie deleteCookie = ResponseCookie.from("chamRefreshToken", "")
                .httpOnly(true)
                .secure(secure)
                .path("/")
                .maxAge(0) // 삭제
                .sameSite("Strict")
                .build();
        response.addHeader("Set-Cookie", deleteCookie.toString());
        return apiResponse;
    }
}
