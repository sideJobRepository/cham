package com.cham.reply.repository;

import com.cham.reply.entity.ChamMonimapReply;
import com.cham.reply.repository.query.ChamMonimapReplyQueryRepository;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChamMonimapReplyRepository extends JpaRepository<ChamMonimapReply, Long>, ChamMonimapReplyQueryRepository {
    

}
