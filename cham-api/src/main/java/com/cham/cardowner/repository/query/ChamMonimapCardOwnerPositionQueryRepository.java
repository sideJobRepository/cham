package com.cham.cardowner.repository.query;

import com.cham.cardowner.entity.ChamMonimapCardOwnerPosition;
import com.cham.dto.response.CardOwnerPositionDto;

import java.util.List;
import java.util.Optional;

public interface ChamMonimapCardOwnerPositionQueryRepository {
    
    
    Optional<ChamMonimapCardOwnerPosition> findByCardOwnerPositionName(String name);
    
    List<CardOwnerPositionDto> findByCardOwnerPositionDtos();
}
