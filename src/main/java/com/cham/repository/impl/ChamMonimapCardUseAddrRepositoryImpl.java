package com.cham.repository.impl;

import com.cham.repository.query.ChamMonimapCardUseAddrQueryRepository;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import static com.cham.entity.QChamMonimapCardUseAddr.chamMonimapCardUseAddr;

@RequiredArgsConstructor
public class ChamMonimapCardUseAddrRepositoryImpl implements ChamMonimapCardUseAddrQueryRepository {
    
    private final JPAQueryFactory queryFactory;
    
    @Override
    public String findByImageUrl(Long cardUseAddrId) {
          return queryFactory.select(chamMonimapCardUseAddr.chamMonimapCardUseImageUrl)
                .from(chamMonimapCardUseAddr)
                .where(chamMonimapCardUseAddr.chamMonimapCardUseAddrId.eq(cardUseAddrId))
                .fetchOne();
    }
}
