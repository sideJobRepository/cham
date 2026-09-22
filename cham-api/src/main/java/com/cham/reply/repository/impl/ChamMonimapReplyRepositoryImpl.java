package com.cham.reply.repository.impl;

import com.cham.reply.entity.ChamMonimapReply;
import com.cham.reply.repository.ChamMonimapReplyRepository;
import com.cham.reply.repository.query.ChamMonimapReplyQueryRepository;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.List;

import static com.cham.carduseaddr.entity.QChamMonimapCardUseAddr.chamMonimapCardUseAddr;
import static com.cham.reply.entity.QChamMonimapReply.chamMonimapReply;

@RequiredArgsConstructor
public class ChamMonimapReplyRepositoryImpl implements ChamMonimapReplyQueryRepository {
    
    private final JPAQueryFactory queryFactory;
    
    @Override
    public List<ChamMonimapReply> findByReplys() {
        return queryFactory
                .selectFrom(chamMonimapReply)
                .join(chamMonimapReply.chamMonimapCardUseAddr, chamMonimapCardUseAddr).fetchJoin()
                .fetch();
    }
}
