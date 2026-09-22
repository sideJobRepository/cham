package com.cham.feedbacck.reply.repository;


import com.cham.feedbacck.reply.entity.LegislationArticleReply;
import com.cham.feedbacck.reply.repository.query.LegislationArticleReplyQueryRepository;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LegislationArticleReplyRepository extends JpaRepository<LegislationArticleReply, Long>, LegislationArticleReplyQueryRepository {
}
