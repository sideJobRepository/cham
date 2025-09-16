package com.cham.refeshtoken.repository.impl;

import com.cham.member.entity.ChamMonimapMember;
import com.cham.refeshtoken.entity.ChamMonimapRefreshToken;
import com.cham.refeshtoken.repository.query.ChamMonimapRefreshTokenQueryRepository;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

import static com.cham.refeshtoken.entity.QChamMonimapRefreshToken.chamMonimapRefreshToken;


@RequiredArgsConstructor
public class ChamMonimapRefreshTokenRepositoryImpl implements ChamMonimapRefreshTokenQueryRepository {

    private final JPAQueryFactory queryFactory;
    
    @Override
    public Optional<ChamMonimapRefreshToken> findByMemberToken(ChamMonimapMember member) {
        ChamMonimapRefreshToken findMember = queryFactory
                .select(chamMonimapRefreshToken)
                .from(chamMonimapRefreshToken)
                .where(chamMonimapRefreshToken.chamMonimapMember.eq(member))
                .fetchFirst();
        return Optional.ofNullable(findMember);
    }
    
    @Override
    public Optional<ChamMonimapRefreshToken> findByTokenValue(String refreshTokenValue) {
        ChamMonimapRefreshToken token = queryFactory
                .selectFrom(chamMonimapRefreshToken)
                .where(chamMonimapRefreshToken.chamMonimapRefreshTokenValue.eq(refreshTokenValue))
                .fetchFirst();
        return Optional.ofNullable(token);
    }
}
