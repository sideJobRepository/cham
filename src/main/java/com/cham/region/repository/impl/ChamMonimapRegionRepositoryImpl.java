package com.cham.region.repository.impl;

import com.cham.region.entity.ChamMonimapRegion;
import com.cham.region.entity.QChamMonimapRegion;
import com.cham.region.repository.query.ChamMonimapRegionQueryRepository;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import static com.cham.region.entity.QChamMonimapRegion.*;

@RequiredArgsConstructor
public class ChamMonimapRegionRepositoryImpl implements ChamMonimapRegionQueryRepository {

    private final JPAQueryFactory queryFactory;
    
    @Override
    public ChamMonimapRegion findByCity(String region1depthName) {
        return queryFactory
                .selectFrom(chamMonimapRegion)
                .where(chamMonimapRegion.chamMonimapRegionName.eq(region1depthName),chamMonimapRegion.chamMonimapRegionDepth.eq(0))
                .fetchFirst();
    }
    
    @Override
    public ChamMonimapRegion findByGu(String region1depthName, String region2depthName) {
        QChamMonimapRegion gu    = new QChamMonimapRegion("gu");
        QChamMonimapRegion city  = new QChamMonimapRegion("city");
        return queryFactory
                .selectFrom(gu)
                .join(gu.parent,  city)
                .where(
                        city.chamMonimapRegionName.eq(region1depthName),
                        gu.chamMonimapRegionName.eq(region2depthName),
                        gu.chamMonimapRegionDepth.eq(1)
                )
                .fetchFirst();
    }
    
    @Override
    public ChamMonimapRegion findByDong(String region1depthName, String region2depthName, String region3depthName) {
        QChamMonimapRegion dong  = new QChamMonimapRegion("dong");
        QChamMonimapRegion gu    = new QChamMonimapRegion("gu");
        QChamMonimapRegion city  = new QChamMonimapRegion("city");
        
        return queryFactory
                .selectFrom(dong)
                .join(dong.parent, gu)
                .join(gu.parent,  city)
                .where(
                        city.chamMonimapRegionName.eq(region1depthName),
                        gu.chamMonimapRegionName.eq(region2depthName),
                        dong.chamMonimapRegionName.eq(region3depthName),
                        dong.chamMonimapRegionDepth.eq(2)
                )
                .fetchFirst();
    }
    
    @Override
    public ChamMonimapRegion findByNameAndDepth(String name, int depth) {
        return queryFactory
                .selectFrom(chamMonimapRegion)
                .where(
                        chamMonimapRegion.chamMonimapRegionName.eq(name),
                        chamMonimapRegion.chamMonimapRegionDepth.eq(depth)
                )
                .fetchFirst();
    }
}
