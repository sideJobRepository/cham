package com.cham.check.repository.impl;

import com.cham.check.dto.CheckLogGetResponse;
import com.cham.check.dto.QCheckLogGetResponse;
import com.cham.check.repository.query.ChamMonimapCheckLogQueryRepository;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import static com.cham.check.entity.QChamMonimapCheckLog.chamMonimapCheckLog;

@RequiredArgsConstructor
public class ChamMonimapCheckLogRepositoryImpl implements ChamMonimapCheckLogQueryRepository {

    private final JPAQueryFactory queryFactory;
    
    @Override
    public CheckLogGetResponse findByCheckAggregation(Long chamMonimapCardUseAddrId) {
        return queryFactory
                .select(
                        new QCheckLogGetResponse(
                                chamMonimapCheckLog.chamMonimapCardUseAddr.chamMonimapCardUseAddrId,
                                chamMonimapCheckLog.chamMonimapVisited.count().as("visitedCnt"),
                                chamMonimapCheckLog.chamMonimapSuspicioused.count().as("suspiciousedCnt")
                        )
                )
                .from(chamMonimapCheckLog)
                .where(chamMonimapCheckLog.chamMonimapCardUseAddr.chamMonimapCardUseAddrId.eq(chamMonimapCardUseAddrId))
                .groupBy(chamMonimapCheckLog.chamMonimapCardUseAddr.chamMonimapCardUseAddrId)
                .fetchFirst();
    }
}
