package com.cham.refeshtoken.repository.query;

import com.cham.member.entity.ChamMonimapMember;
import com.cham.refeshtoken.entity.ChamMonimapRefreshToken;

import java.util.Optional;

public interface ChamMonimapRefreshTokenQueryRepository {
    
    Optional<ChamMonimapRefreshToken> findByChamMonimapMember(ChamMonimapMember member);
    Optional<ChamMonimapRefreshToken> findByChamMonimapRefreshTokenValue(String refreshTokenValue);
}
