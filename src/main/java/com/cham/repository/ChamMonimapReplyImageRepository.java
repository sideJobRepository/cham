package com.cham.repository;

import com.cham.entity.ChamMonimapReplyImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ChamMonimapReplyImageRepository extends JpaRepository<ChamMonimapReplyImage, Long> {
    
    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM ChamMonimapReplyImage R WHERE R.chamMonimapReplyImageUrl = :imageUrl")
    void deletebyImageUrl(@Param("imageUrl") String imageUrl);
    
    @Query("SELECT R.chamMonimapReplyImageUrl FROM ChamMonimapReplyImage R WHERE R.chamMonimapReply.chamMonimapReplyId = :replyId")
    List<String> findByReplyImageUrlInReplyId(@Param("replyId") Long replyId);
    
    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM ChamMonimapReplyImage R WHERE R.chamMonimapReply.chamMonimapReplyId = :replyId")
    void deletebyReplyImage(@Param("replyId") Long replyId);
    
}
