package com.cham.repository;

import com.cham.entity.ReplyImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReplyImageRepository extends JpaRepository<ReplyImage, Long> {
    
    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM ReplyImage R WHERE R.replyImageUrl = :imageUrl")
    void deletebyImageUrl(@Param("imageUrl") String imageUrl);
    
    @Query("SELECT R.replyImageUrl FROM ReplyImage R WHERE R.reply.replyId = :replyId")
    List<String> findByReplyImageUrlInReplyId(@Param("replyId") Long replyId);
    
    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM ReplyImage R WHERE R.reply.replyId = :replyId")
    void deletebyReplyImage(@Param("replyId") Long replyId);
    
}
