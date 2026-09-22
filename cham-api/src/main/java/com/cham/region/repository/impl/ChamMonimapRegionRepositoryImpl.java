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
