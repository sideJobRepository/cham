package com.cham.feedbacck.great.repository.impl;

import com.cham.feedbacck.great.dto.response.GreatMyTypeProjection;
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
    public List<GreatTypeCount> findGreatCounts(List<Long> articleIds) {
        return queryFactory
                .select(Projections.constructor(
                        GreatTypeCount.class,
                        great.legislationArticle.id, // articleId
                        great.greatType,              // greatType
                        great.count()                 // count
                ))
                .from(great)
                .where(great.legislationArticle.id.in(articleIds))
                .groupBy(
                        great.legislationArticle.id,
                        great.greatType
                )
                .fetch();
    }
    
    @Override
    public List<GreatMyTypeProjection> findMyGreatType(List<Long> articleIds, Long memberId) {
        return queryFactory
                .select(Projections.constructor(
                        GreatMyTypeProjection.class,
                        great.legislationArticle.id,
                        great.greatType
                ))
                .from(great)
                .where(
                        great.legislationArticle.id.in(articleIds),
                        great.member.chamMonimapMemberId.eq(memberId)
                )
                .fetch();
    }
}
