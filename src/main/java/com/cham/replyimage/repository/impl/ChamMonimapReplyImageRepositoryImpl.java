package com.cham.replyimage.repository.impl;

import com.cham.replyimage.repository.query.ChamMonimapReplyImageQueryRepository;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

import java.util.List;

import static com.cham.replyimage.entity.QChamMonimapReplyImage.*;


@RequiredArgsConstructor
public class ChamMonimapReplyImageRepositoryImpl implements ChamMonimapReplyImageQueryRepository {
    
    
    private final JPAQueryFactory queryFactory;
    
    private final EntityManager em;
    
    
    @Override
    public void deleteByImageUrl(String imageUrl) {
        em.flush();
        queryFactory
                .delete(chamMonimapReplyImage)
                .where(chamMonimapReplyImage.chamMonimapReplyImageUrl.eq(imageUrl))
                .execute();
        em.clear();
    }
    
    @Override
    public List<String> findByReplyImageUrlInReplyId(Long replyId) {
        return queryFactory
                .select(chamMonimapReplyImage.chamMonimapReplyImageUrl)
                .from(chamMonimapReplyImage)
                .where(chamMonimapReplyImage.chamMonimapReply.chamMonimapReplyId.eq(replyId))
                .fetch();
    }
    
    @Override
    public void deleteByReplyImage(Long replyId) {
        em.flush();
        queryFactory
                .delete(chamMonimapReplyImage)
                .where(chamMonimapReplyImage.chamMonimapReply.chamMonimapReplyId.eq(replyId))
                .execute();
        em.clear();
    }
}
