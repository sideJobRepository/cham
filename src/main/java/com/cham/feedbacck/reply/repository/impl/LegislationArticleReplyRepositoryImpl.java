package com.cham.feedbacck.reply.repository.impl;

import com.cham.feedbacck.legislationarticle.entity.QLegislationArticle;
import com.cham.feedbacck.reply.entity.LegislationArticleReply;
import com.cham.feedbacck.reply.entity.QLegislationArticleReply;
import com.cham.feedbacck.reply.repository.query.LegislationArticleReplyQueryRepository;
import com.cham.member.entity.QChamMonimapMember;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.List;

import static com.cham.feedbacck.legislationarticle.entity.QLegislationArticle.*;
import static com.cham.feedbacck.reply.entity.QLegislationArticleReply.*;
import static com.cham.member.entity.QChamMonimapMember.*;


@RequiredArgsConstructor
public class LegislationArticleReplyRepositoryImpl implements LegislationArticleReplyQueryRepository {
    
    private final JPAQueryFactory queryFactory;
    
    @Override
    public List<LegislationArticleReply> findRepliesByArticleId(Long articleId) {
        return queryFactory
                .selectFrom(legislationArticleReply)
                .join(legislationArticleReply.member, chamMonimapMember).fetchJoin()
                .where(legislationArticleReply.article.id.eq(articleId))
                .orderBy(
                        legislationArticleReply.parent.id.asc().nullsFirst(),
                        legislationArticleReply.registDate.asc()
                )
                .fetch();
    }
}
