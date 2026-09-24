package com.cham.collect.repository;

import com.cham.collect.entity.ChamMonimapCollectJob;
import com.cham.collect.repository.query.ChamMonimapCollectJobQueryRepository;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChamMonimapCollectJobRepository extends JpaRepository<ChamMonimapCollectJob, Long>, ChamMonimapCollectJobQueryRepository {
}
