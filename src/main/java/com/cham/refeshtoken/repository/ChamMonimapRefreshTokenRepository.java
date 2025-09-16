package com.cham.refeshtoken.repository;

import com.cham.refeshtoken.entity.ChamMonimapRefreshToken;
import com.cham.refeshtoken.repository.query.ChamMonimapRefreshTokenQueryRepository;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChamMonimapRefreshTokenRepository extends JpaRepository<ChamMonimapRefreshToken, Long>, ChamMonimapRefreshTokenQueryRepository {

}
