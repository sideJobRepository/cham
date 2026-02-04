package com.cham.feedbacck.reply.repository.query;

import com.cham.feedbacck.legislationarticle.entity.LegislationArticle;
import com.cham.feedbacck.reply.entity.LegislationArticleReply;

import java.util.List;
import java.util.Map;

public interface LegislationArticleReplyQueryRepository {
    
     List<LegislationArticleReply> findRepliesByArticleId(Long articleId);
     
     
     
     List<Object[]> countByArticles(List<LegislationArticle> articles);
    
    List<LegislationArticleReply> findLegislationReplies(Long legislationId);
    
    Long countLegislationReplies(Long legislationId);
}
