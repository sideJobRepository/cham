package com.cham.collect.repository;

import com.cham.collect.entity.ChamMonimapCollectFile;
import com.cham.collect.repository.query.ChamMonimapCollectFileQueryRepository;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ChamMonimapCollectFileRepository extends JpaRepository<ChamMonimapCollectFile, Long>, ChamMonimapCollectFileQueryRepository {

    // 반영 버튼을 두 번 눌러도 한 번만 들어가도록 행을 잠근다
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select f from ChamMonimapCollectFile f join fetch f.collectSource where f.chamMonimapCollectFileId = :id")
    Optional<ChamMonimapCollectFile> findByIdForUpdate(@Param("id") Long id);

    @Query("select f from ChamMonimapCollectFile f join fetch f.collectSource where f.chamMonimapCollectFileId = :id")
    Optional<ChamMonimapCollectFile> findWithSource(@Param("id") Long id);
}
