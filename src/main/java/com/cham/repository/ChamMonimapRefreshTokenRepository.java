package com.cham.repository;

import com.cham.entity.ChamMonimapRefreshToken;
import com.cham.repository.query.ChamMonimapRefreshTokenQueryRepository;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChamMonimapRefreshTokenRepository extends JpaRepository<ChamMonimapRefreshToken, Long>, ChamMonimapRefreshTokenQueryRepository {

}
