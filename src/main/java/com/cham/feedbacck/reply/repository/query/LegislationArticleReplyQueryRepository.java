package com.cham.feedbacck.reply.repository.query;

import com.cham.feedbacck.legislationarticle.entity.LegislationArticle;
import com.cham.feedbacck.reply.entity.LegislationArticleReply;

import java.util.List;
import java.util.Map;

public interface LegislationArticleReplyQueryRepository {
    
     List<LegislationArticleReply> findRepliesByArticleId(Long articleId);
     
     Map<Long, Long> findReplyCountMapByArticleIds(List<Long> articleIds);
     
     List<Object[]> countByArticles(List<LegislationArticle> articles);
}
