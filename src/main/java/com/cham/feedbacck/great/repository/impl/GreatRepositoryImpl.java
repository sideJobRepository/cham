package com.cham.feedbacck.great.repository.impl;

import com.cham.feedbacck.great.dto.response.GreatTypeCount;
import com.cham.feedbacck.great.enums.GreatType;
import com.cham.feedbacck.great.repository.query.GreatQueryRepository;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.List;

import static com.cham.feedbacck.great.entity.QGreat.great;

@RequiredArgsConstructor
public class GreatRepositoryImpl implements GreatQueryRepository {
    
    private final JPAQueryFactory queryFactory;
    
    
    @Override
    public List<GreatTypeCount> findGreatCounts(Long articleId) {
        return queryFactory
                .select(Projections.constructor(
                        GreatTypeCount.class,
                        great.greatType,
                        great.count()
                ))
                .from(great)
                .where(great.legislationArticle.id.eq(articleId))
                .groupBy(great.greatType)
                .fetch();
    }
    
    @Override
    public GreatType findMyGreatType(Long articleId, Long memberId) {
        return queryFactory
                .select(great.greatType)
                .from(great)
                .where(
                        great.legislationArticle.id.eq(articleId),
                        great.member.chamMonimapMemberId.eq(memberId)
                )
                .fetchOne();
    }
}
