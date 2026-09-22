package com.cham.region.repository;

import com.cham.region.entity.ChamMonimapRegion;
import com.cham.region.repository.query.ChamMonimapRegionQueryRepository;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChamMonimapRegionRepository extends JpaRepository<ChamMonimapRegion, Long> , ChamMonimapRegionQueryRepository {
}
