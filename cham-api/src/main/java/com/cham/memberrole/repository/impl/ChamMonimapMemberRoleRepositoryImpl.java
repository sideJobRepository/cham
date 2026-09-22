package com.cham.memberrole.repository.impl;

import com.cham.memberrole.dto.ChamMemberRoleGetResponse;
import com.cham.memberrole.entity.ChamMonimapMemberRole;
import com.cham.memberrole.repository.query.ChamMonimapMemberRoleQueryRepository;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.CaseBuilder;
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
    public Page<ChamMemberRoleGetResponse> findByMemberRoles(Pageable pageable) {
        List<ChamMemberRoleGetResponse> result = queryFactory
                .select(
                        Projections.constructor(
                                ChamMemberRoleGetResponse.class,
                                chamMonimapMemberRole.chamMonimapMember.chamMonimapMemberId,
                                chamMonimapMemberRole.chamMonimapRole.chamMonimapRoleId,
                                chamMonimapMemberRole.chamMonimapMemberRoleId,
                                chamMonimapMember.chamMonimapMemberName,
                                chamMonimapRole.chamMonimapRoleName,
                                chamMonimapMember.chamMonimapMemberEmail,
                                chamMonimapMember.chamMonimapMemberPhoneNo
                        )
                )
                .from(chamMonimapMemberRole)
                .join(chamMonimapMemberRole.chamMonimapMember, chamMonimapMember)
                .join(chamMonimapMemberRole.chamMonimapRole, chamMonimapRole)
                .orderBy(
                        new CaseBuilder()
                                .when(chamMonimapRole.chamMonimapRoleName.eq("ADMIN")).then(0)
                                .otherwise(1).asc(),
                        chamMonimapMember.chamMonimapMemberId.asc()
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
        
        JPAQuery<Long> countQuery = queryFactory
                .select(chamMonimapMember.count())
                .from(chamMonimapMember);
        return PageableExecutionUtils.getPage(result,pageable,countQuery::fetchOne);
    }
    
}
