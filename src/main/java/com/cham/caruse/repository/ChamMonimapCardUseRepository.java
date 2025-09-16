package com.cham.caruse.repository;

import com.cham.caruse.entity.ChamMonimapCardUse;
import com.cham.caruse.repository.query.ChamMonimapCardUseQueryRepository;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChamMonimapCardUseRepository extends JpaRepository<ChamMonimapCardUse, Long> , ChamMonimapCardUseQueryRepository {

}

