package com.cham.repository;

import com.cham.entity.ReplyImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReplyImageRepository extends JpaRepository<ReplyImage, Long> {
    
    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM ReplyImage R WHERE R.replyImageUrl = :imageUrl")
    void deletebyImageUrl(@Param("imageUrl") String imageUrl);
    
}
