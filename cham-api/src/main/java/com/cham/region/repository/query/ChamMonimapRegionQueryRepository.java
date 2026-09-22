package com.cham.region.repository.query;

import com.cham.region.entity.ChamMonimapRegion;

public interface ChamMonimapRegionQueryRepository {
    
    
    ChamMonimapRegion findByNameAndDepth(String name, int depth);
    
    
}
