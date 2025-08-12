package com.cham.service;

import com.cham.controller.response.ApiResponse;
import com.cham.entity.ChamMonimapMember;
import com.cham.security.dto.TokenAndUser;

import java.time.LocalDateTime;

public interface ChamMonimapRefreshTokenService {
    void refreshTokenSaveOrUpdate(ChamMonimapMember member, String refreshTokenValue, LocalDateTime expiresAt);
    ChamMonimapMember validateRefreshToken(String refreshToken);
    TokenAndUser reissueTokenWithUser(String refreshToken); // ← 이름/시그니처 통일
    ApiResponse deleteRefresh(String refreshToken);
}
