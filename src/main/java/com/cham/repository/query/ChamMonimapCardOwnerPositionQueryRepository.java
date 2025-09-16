package com.cham.repository.query;

import com.cham.entity.ChamMonimapCardOwnerPosition;

import java.util.Optional;

public interface ChamMonimapCardOwnerPositionQueryRepository {
    
    
    Optional<ChamMonimapCardOwnerPosition> findByCardOwnerPositionName(String name);
}
