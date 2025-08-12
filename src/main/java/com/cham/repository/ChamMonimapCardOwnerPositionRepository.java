package com.cham.repository;

import com.cham.entity.ChamMonimapCardOwnerPosition;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ChamMonimapCardOwnerPositionRepository extends JpaRepository<ChamMonimapCardOwnerPosition, Long> {
    
    Optional<ChamMonimapCardOwnerPosition> findByChamMonimapCardOwnerPositionName(String name);
}
