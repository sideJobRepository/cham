package com.cham.repository;

import com.cham.entity.ChamMonimapMember;
import com.cham.entity.ChamMonimapRefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ChamMonimapRefreshTokenRepository extends JpaRepository<ChamMonimapRefreshToken, Long> {

    
    Optional<ChamMonimapRefreshToken> findByChamMonimapMember(ChamMonimapMember member);
    Optional<ChamMonimapRefreshToken> findByChamMonimapRefreshTokenValue(String refreshTokenValue);
}
