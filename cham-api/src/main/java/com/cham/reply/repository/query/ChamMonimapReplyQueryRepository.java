package com.cham.reply.repository.query;

import com.cham.reply.entity.ChamMonimapReply;

import java.util.List;

public interface ChamMonimapReplyQueryRepository {
    
    List<ChamMonimapReply> findByReplys();
}
