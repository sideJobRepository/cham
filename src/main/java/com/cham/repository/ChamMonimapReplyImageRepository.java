package com.cham.repository;

import com.cham.entity.ChamMonimapReplyImage;
import com.cham.repository.query.ChamMonimapReplyImageQueryRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ChamMonimapReplyImageRepository extends JpaRepository<ChamMonimapReplyImage, Long>, ChamMonimapReplyImageQueryRepository {
    

}
