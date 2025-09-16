package com.cham.repository.impl;


import com.cham.entity.ChamMonimapMember;
import com.cham.repository.query.ChamMonimapMemberQueryRepository;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

import static com.cham.entity.QChamMonimapMember.chamMonimapMember;

@RequiredArgsConstructor
public class ChamMonimapMemberRepositoryImpl implements ChamMonimapMemberQueryRepository {
    
    
    private final JPAQueryFactory queryFactory;
    
    @Override
    public Optional<ChamMonimapMember> findByChamMonimapMemberSubId(String subId) {
        ChamMonimapMember member = queryFactory
                .selectFrom(chamMonimapMember)
                .where(chamMonimapMember.chamMonimapMemberSubId.eq(subId))
                .fetchFirst();
        return Optional.ofNullable(member);
    }
}
