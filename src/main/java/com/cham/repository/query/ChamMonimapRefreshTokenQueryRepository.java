package com.cham.repository.query;

import com.cham.entity.ChamMonimapMember;
import com.cham.entity.ChamMonimapRefreshToken;

import java.util.Optional;

public interface ChamMonimapRefreshTokenQueryRepository {
    
    Optional<ChamMonimapRefreshToken> findByChamMonimapMember(ChamMonimapMember member);
    Optional<ChamMonimapRefreshToken> findByChamMonimapRefreshTokenValue(String refreshTokenValue);
}
