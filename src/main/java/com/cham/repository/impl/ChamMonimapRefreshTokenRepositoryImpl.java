package com.cham.repository.impl;

import com.cham.entity.ChamMonimapMember;
import com.cham.entity.ChamMonimapRefreshToken;
import com.cham.entity.QChamMonimapRefreshToken;
import com.cham.repository.query.ChamMonimapRefreshTokenQueryRepository;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

import static com.cham.entity.QChamMonimapRefreshToken.*;

@RequiredArgsConstructor
public class ChamMonimapRefreshTokenRepositoryImpl implements ChamMonimapRefreshTokenQueryRepository {

    private final JPAQueryFactory queryFactory;
    
    @Override
    public Optional<ChamMonimapRefreshToken> findByChamMonimapMember(ChamMonimapMember member) {
        ChamMonimapRefreshToken findMember = queryFactory
                .select(chamMonimapRefreshToken)
                .from(chamMonimapRefreshToken)
                .where(chamMonimapRefreshToken.chamMonimapMember.eq(member))
                .fetchFirst();
        return Optional.ofNullable(findMember);
    }
    
    @Override
    public Optional<ChamMonimapRefreshToken> findByChamMonimapRefreshTokenValue(String refreshTokenValue) {
        ChamMonimapRefreshToken token = queryFactory
                .selectFrom(chamMonimapRefreshToken)
                .where(chamMonimapRefreshToken.chamMonimapRefreshTokenValue.eq(refreshTokenValue))
                .fetchFirst();
        return Optional.ofNullable(token);
    }
}
