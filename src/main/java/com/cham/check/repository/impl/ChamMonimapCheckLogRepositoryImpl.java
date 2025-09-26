package com.cham.check.repository.impl;


import com.cham.check.dto.response.CheckLogGetResponse;
import com.cham.check.dto.response.QCheckLogGetResponse;
import com.cham.check.entity.ChamMonimapCheckLog;
import com.cham.check.entity.QChamMonimapCheckLog;
import com.cham.check.repository.query.ChamMonimapCheckLogQueryRepository;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.core.types.dsl.StringExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import static com.cham.check.entity.QChamMonimapCheckLog.chamMonimapCheckLog;

@RequiredArgsConstructor
public class ChamMonimapCheckLogRepositoryImpl implements ChamMonimapCheckLogQueryRepository {

    private final JPAQueryFactory queryFactory;
    
    @Override
    public CheckLogGetResponse findByCheckAggregation(Long chamMonimapCardUseAddrId,Long memberId) {
        if(memberId == null) {
            CheckLogGetResponse checkLogGetResponse = queryFactory
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
            if (checkLogGetResponse == null) {
                return new CheckLogGetResponse(chamMonimapCardUseAddrId,0L,0L,null,null);
            }
            return checkLogGetResponse;
        }
        
        CheckLogGetResponse checkLogGetResponse = queryFactory
                .select(
                        new QCheckLogGetResponse(
                                chamMonimapCheckLog.chamMonimapCardUseAddr.chamMonimapCardUseAddrId,
                                chamMonimapCheckLog.chamMonimapVisited.count().as("visitedCnt"),
                                chamMonimapCheckLog.chamMonimapSuspicioused.count().as("suspiciousedCnt"),
                                ExpressionUtils.as(
                                        JPAExpressions.select(chamMonimapCheckLog.chamMonimapVisited)
                                                .from(chamMonimapCheckLog)
                                                .where(
                                                        chamMonimapCheckLog.chamMonimapMember.chamMonimapMemberId.eq(memberId),
                                                        chamMonimapCheckLog.chamMonimapCardUseAddr.chamMonimapCardUseAddrId.eq(chamMonimapCardUseAddrId)
                                                ),
                                        "myVisited"
                                ),
                                ExpressionUtils.as(
                                        JPAExpressions.select(chamMonimapCheckLog.chamMonimapSuspicioused)
                                                .from(chamMonimapCheckLog)
                                                .where(
                                                        chamMonimapCheckLog.chamMonimapMember.chamMonimapMemberId.eq(memberId),
                                                        chamMonimapCheckLog.chamMonimapCardUseAddr.chamMonimapCardUseAddrId.eq(chamMonimapCardUseAddrId)
                                                ),
                                        "mySpicioused"
                                )
                        )
                )
                .from(chamMonimapCheckLog)
                .where(chamMonimapCheckLog.chamMonimapCardUseAddr.chamMonimapCardUseAddrId.eq(chamMonimapCardUseAddrId))
                .groupBy(chamMonimapCheckLog.chamMonimapCardUseAddr.chamMonimapCardUseAddrId)
                .fetchFirst();
        
        if (checkLogGetResponse == null) {
            return new CheckLogGetResponse(chamMonimapCardUseAddrId,0L,0L,null,null);
        }
        return checkLogGetResponse;
        
    }
    
    @Override
    public ChamMonimapCheckLog findByCheckLog(Long memberId, Long addrId) {
        return queryFactory
                .selectFrom(chamMonimapCheckLog)
                .where(chamMonimapCheckLog.chamMonimapMember.chamMonimapMemberId.eq(memberId),
                        chamMonimapCheckLog.chamMonimapCardUseAddr.chamMonimapCardUseAddrId.eq(addrId)
                )
                .fetchFirst();
    }
}
