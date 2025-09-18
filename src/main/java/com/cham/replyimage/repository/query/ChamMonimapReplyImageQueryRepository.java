package com.cham.replyimage.repository.query;

import com.cham.replyimage.entity.ChamMonimapReplyImage;

import java.util.List;

public interface ChamMonimapReplyImageQueryRepository {
    
    
    void deleteByImageUrl(String imageUrl);
    
    List<String> findByReplyImageUrlInReplyId(Long replyId);
    
    void deleteByReplyImage(Long replyId);
    
    List<ChamMonimapReplyImage> findByReplyImages();
}
