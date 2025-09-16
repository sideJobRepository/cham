package com.cham.role.repository.impl;

import com.cham.role.entity.ChamMonimapRole;
import com.cham.role.repository.query.ChamMonimapRoleQueryRepository;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import static com.cham.role.entity.QChamMonimapRole.chamMonimapRole;

@RequiredArgsConstructor
@Repository
public class ChamMonimapRoleRepositoryImpl implements ChamMonimapRoleQueryRepository {
    
    private final JPAQueryFactory queryFactory;
    
    @Override
    public ChamMonimapRole findByMemberRoleName(String chamMonimapRoleName) {
        return queryFactory
                .selectFrom(chamMonimapRole)
                .where(chamMonimapRole.chamMonimapRoleName.eq(chamMonimapRoleName))
                .fetchOne();
    }
}
