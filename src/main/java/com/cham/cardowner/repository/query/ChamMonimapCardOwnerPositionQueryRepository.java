package com.cham.cardowner.repository.query;

import com.cham.cardowner.entity.ChamMonimapCardOwnerPosition;

import java.util.Optional;

public interface ChamMonimapCardOwnerPositionQueryRepository {
    
    
    Optional<ChamMonimapCardOwnerPosition> findByCardOwnerPositionName(String name);
}
