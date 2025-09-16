package com.cham.member.repository.impl;


import com.cham.member.entity.ChamMonimapMember;
import com.cham.member.repository.query.ChamMonimapMemberQueryRepository;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

import static com.cham.member.entity.QChamMonimapMember.chamMonimapMember;


@RequiredArgsConstructor
public class ChamMonimapMemberRepositoryImpl implements ChamMonimapMemberQueryRepository {
    
    
    private final JPAQueryFactory queryFactory;
    
    @Override
    public Optional<ChamMonimapMember> findByMemberSubId(String subId) {
        ChamMonimapMember member = queryFactory
                .selectFrom(chamMonimapMember)
                .where(chamMonimapMember.chamMonimapMemberSubId.eq(subId))
                .fetchFirst();
        return Optional.ofNullable(member);
    }
}
