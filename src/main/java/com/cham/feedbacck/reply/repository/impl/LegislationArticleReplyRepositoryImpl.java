package com.cham.feedbacck.reply.repository.impl;

import com.cham.feedbacck.reply.entity.LegislationArticleReply;
import com.cham.feedbacck.reply.repository.query.LegislationArticleReplyQueryRepository;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.List;

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
}
