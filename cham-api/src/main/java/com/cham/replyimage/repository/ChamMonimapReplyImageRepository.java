package com.cham.replyimage.repository;

import com.cham.replyimage.entity.ChamMonimapReplyImage;
import com.cham.replyimage.repository.query.ChamMonimapReplyImageQueryRepository;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChamMonimapReplyImageRepository extends JpaRepository<ChamMonimapReplyImage, Long>, ChamMonimapReplyImageQueryRepository {
    

}
