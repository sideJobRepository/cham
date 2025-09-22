package com.cham.memberrole.repository.impl;

import com.cham.memberrole.entity.ChamMonimapMemberRole;
import com.cham.memberrole.repository.query.ChamMonimapMemberRoleQueryRepository;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import static com.cham.member.entity.QChamMonimapMember.chamMonimapMember;
import static com.cham.memberrole.entity.QChamMonimapMemberRole.chamMonimapMemberRole;
import static com.cham.role.entity.QChamMonimapRole.chamMonimapRole;


@RequiredArgsConstructor
@Repository
public class ChamMonimapMemberRoleRepositoryImpl implements ChamMonimapMemberRoleQueryRepository {

    private final JPAQueryFactory queryFactory;
    
    
    @Override
    public Optional<ChamMonimapMemberRole> findByMemberRole(Long memberId) {
        ChamMonimapMemberRole result = queryFactory
                .selectFrom(chamMonimapMemberRole)
                .where(chamMonimapMemberRole.chamMonimapMember.chamMonimapMemberId.eq(memberId))
                .fetchOne();
        return Optional.ofNullable(result);
    }
    
    @Override
    public List<String> findByRoleName(Long id) {
        return queryFactory
                .select(chamMonimapRole.chamMonimapRoleName)
                .from(chamMonimapMemberRole)
                .join(chamMonimapMemberRole.chamMonimapMember, chamMonimapMember)
                .join(chamMonimapMemberRole.chamMonimapRole, chamMonimapRole)
                .where(chamMonimapMemberRole.chamMonimapMember.chamMonimapMemberId.eq(id))
                .fetch();
    }
    
    @Override
    public Page<ChamMonimapMemberRole> findByMemberRoles(Pageable pageable) {
        List<ChamMonimapMemberRole> result = queryFactory
                .select(chamMonimapMemberRole)
                .from(chamMonimapMemberRole)
                .join(chamMonimapMemberRole.chamMonimapMember, chamMonimapMember).fetchJoin()
                .join(chamMonimapMemberRole.chamMonimapRole, chamMonimapRole).fetchJoin()
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
        
        JPAQuery<Long> countQuery = queryFactory
                .select(chamMonimapMemberRole.count())
                .from(chamMonimapMemberRole)
                .join(chamMonimapMemberRole.chamMonimapMember, chamMonimapMember).fetchJoin()
                .join(chamMonimapMemberRole.chamMonimapRole, chamMonimapRole).fetchJoin();
        return PageableExecutionUtils.getPage(result,pageable,countQuery::fetchOne);
    }
    
}
