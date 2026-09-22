package com.cham.file.repository;

import com.cham.file.entity.ChamMonimapCommonFile;
import com.cham.file.repository.query.ChamMonimapCommonFileQueryRepository;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChamMonimapCommonFileRepository extends JpaRepository<ChamMonimapCommonFile, Long>, ChamMonimapCommonFileQueryRepository {
}
