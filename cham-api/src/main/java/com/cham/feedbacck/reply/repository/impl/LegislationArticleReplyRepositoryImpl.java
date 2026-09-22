package com.cham.feedbacck.reply.repository.impl;

import com.cham.feedbacck.legislation.entity.QLegislation;
import com.cham.feedbacck.legislationarticle.entity.LegislationArticle;
import com.cham.feedbacck.reply.entity.LegislationArticleReply;
import com.cham.feedbacck.reply.repository.query.LegislationArticleReplyQueryRepository;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.cham.feedbacck.legislation.entity.QLegislation.*;
import static com.cham.feedbacck.legislationarticle.entity.QLegislationArticle.legislationArticle;
import static com.cham.feedbacck.reply.entity.QLegislationArticleReply.legislationArticleReply;
import static com.cham.member.entity.QChamMonimapMember.chamMonimapMember;


@RequiredArgsConstructor
public class LegislationArticleReplyRepositoryImpl implements LegislationArticleReplyQueryRepository {
    
    private final JPAQueryFactory queryFactory;
    
    @Override
    public List<LegislationArticleReply> findRepliesByArticleId(Long articleId) {
        return queryFactory
                .selectFrom(legislationArticleReply)
                .join(legislationArticleReply.member, chamMonimapMember).fetchJoin()
                .join(legislationArticleReply.article,legislationArticle).fetchJoin()
                .where(legislationArticleReply.article.id.eq(articleId))
                .orderBy(
                        legislationArticleReply.parent.id.asc().nullsFirst(),
                        legislationArticleReply.registDate.asc()
                )
                .fetch();
    }
    
    @Override
    public List<Object[]> countByArticles(List<LegislationArticle> articles) {
      
        if (articles == null || articles.isEmpty()) {
            return List.of();
        }
    
        return queryFactory
                .select(
                        legislationArticleReply.article.id,
                        legislationArticleReply.count()
                )
                .from(legislationArticleReply)
                .where(legislationArticleReply.article.in(articles))
                .groupBy(legislationArticleReply.article.id)
                .fetch()
                .stream()
                .map(tuple -> new Object[] {
                        tuple.get(legislationArticleReply.article.id),
                        tuple.get(legislationArticleReply.count())
                })
                .toList();
    }
    
    @Override
    public List<LegislationArticleReply> findLegislationReplies(Long legislationId) {
        return queryFactory
                .selectFrom(legislationArticleReply)
                .join(legislationArticleReply.member, chamMonimapMember).fetchJoin()
                .join(legislationArticleReply.legislation, legislation).fetchJoin()
                .where(
                        legislationArticleReply.legislation.id.eq(legislationId),
                        legislationArticleReply.article.isNull()
                )
                .orderBy(
                        legislationArticleReply.parent.id.asc().nullsFirst(),
                        legislationArticleReply.registDate.asc()
                )
                .fetch();
    }
    
    @Override
    public Long countLegislationReplies(Long legislationId) {
        return queryFactory
             .select(legislationArticleReply.count())
             .from(legislationArticleReply)
             .where(
                 legislationArticleReply.legislation.id.eq(legislationId),
                 legislationArticleReply.article.isNull()
             )
             .fetchOne();
    }
    
}
