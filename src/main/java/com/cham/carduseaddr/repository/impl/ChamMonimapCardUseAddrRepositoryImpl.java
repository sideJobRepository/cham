package com.cham.carduseaddr.repository.impl;

import com.cham.carduseaddr.repository.query.ChamMonimapCardUseAddrQueryRepository;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import static com.cham.carduseaddr.entity.QChamMonimapCardUseAddr.chamMonimapCardUseAddr;


@RequiredArgsConstructor
@Repository
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
