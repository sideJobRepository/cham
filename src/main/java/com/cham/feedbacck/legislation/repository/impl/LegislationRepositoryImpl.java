package com.cham.feedbacck.legislation.repository.impl;

import com.cham.feedbacck.legislation.repository.query.LegislationQueryRepository;
import com.cham.feedbacck.legislationarticle.entity.LegislationArticle;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.List;

import static com.cham.feedbacck.legislationarticle.entity.QLegislationArticle.legislationArticle;

@RequiredArgsConstructor
public class LegislationRepositoryImpl implements LegislationQueryRepository {
    
    private final JPAQueryFactory queryFactory;
    
    @Override
    public List<LegislationArticle> searchArticlesByKeyword(String keyword) {
        
        return queryFactory
                .selectFrom(legislationArticle)
                .join(legislationArticle.legislation).fetchJoin()
                .where(
                        legislationArticle.cont.containsIgnoreCase(keyword)
                                .or(legislationArticle.articleTitle.containsIgnoreCase(keyword))
                )
                .orderBy(legislationArticle.ordersNo.asc())
                .fetch();
    }
}
