package com.cham.repository.impl;

import com.cham.entity.QChamMonimapCardUse;
import com.cham.repository.query.ChamMonimapCardUseQueryRepository;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import static com.cham.entity.QChamMonimapCardUse.*;

@RequiredArgsConstructor
public class ChamMonimapCardUseRepositoryImpl implements ChamMonimapCardUseQueryRepository {
    
    private final JPAQueryFactory queryFactory;
    
    @Override
    public boolean existsByChamMonimapCardUseDelkey(String cardUseDelkey) {
        
        Integer fetchOne = queryFactory
                .selectOne()
                .from(chamMonimapCardUse)
                .where(chamMonimapCardUse.chamMonimapCardUseDelkey.eq(cardUseDelkey))
                .fetchFirst();
        
        return fetchOne != null;
    }
    
    @Override
    public void deleteByCardUseDelkey(String cardUseDelkey) {
        queryFactory
                .delete(chamMonimapCardUse)
                .where(chamMonimapCardUse.chamMonimapCardUseDelkey.eq(cardUseDelkey))
                .execute();
    }
}
