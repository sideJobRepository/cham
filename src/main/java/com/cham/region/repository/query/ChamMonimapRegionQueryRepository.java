package com.cham.region.repository.query;

import com.cham.region.entity.ChamMonimapRegion;

public interface ChamMonimapRegionQueryRepository {
    
    
    ChamMonimapRegion findByCity(String region1depthName);
    
    ChamMonimapRegion findByGu(String region1depthName,String region2depthName);
    
    ChamMonimapRegion findByDong(String region1depthName,String region2depthName,String region3depthName);
}
