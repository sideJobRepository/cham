package com.cham.check.repository;

import com.cham.check.entity.ChamMonimapCheckLog;
import com.cham.check.repository.query.ChamMonimapCheckLogQueryRepository;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChamMonimapCheckLogRepository extends JpaRepository<ChamMonimapCheckLog, Long>, ChamMonimapCheckLogQueryRepository {
}
