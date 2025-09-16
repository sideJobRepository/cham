package com.cham.repository;

import com.cham.entity.ChamMonimapCardUse;
import com.cham.repository.query.ChamMonimapCardUseQueryRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChamMonimapCardUseRepository extends JpaRepository<ChamMonimapCardUse, Long> , ChamMonimapCardUseQueryRepository {

}

