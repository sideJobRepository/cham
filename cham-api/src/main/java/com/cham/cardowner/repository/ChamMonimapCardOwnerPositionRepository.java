package com.cham.cardowner.repository;

import com.cham.cardowner.entity.ChamMonimapCardOwnerPosition;
import com.cham.cardowner.repository.query.ChamMonimapCardOwnerPositionQueryRepository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ChamMonimapCardOwnerPositionRepository extends JpaRepository<ChamMonimapCardOwnerPosition, Long>, ChamMonimapCardOwnerPositionQueryRepository {
    
    
    Optional<ChamMonimapCardOwnerPosition> findByCardOwnerPositionName(String name);
}
