package com.cham.feedbacck.reply.repository.query;

import com.cham.feedbacck.reply.entity.LegislationArticleReply;

import java.util.List;

public interface LegislationArticleReplyQueryRepository {
    
     List<LegislationArticleReply> findRepliesByArticleId(Long articleId);
     
     Long findReplyCount(Long articleId);
}
