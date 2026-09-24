package com.cham.collect.repository;

import com.cham.collect.entity.ChamMonimapCollectSource;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChamMonimapCollectSourceRepository extends JpaRepository<ChamMonimapCollectSource, Long> {

    List<ChamMonimapCollectSource> findAllByOrderByChamMonimapCollectSourceSortAscChamMonimapCollectSourceIdAsc();
}
