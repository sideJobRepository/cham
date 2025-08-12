package com.cham.repository;

import com.cham.entity.ChamMonimapCardUseAddr;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChamMonimapCardUseAddrRepository extends JpaRepository<ChamMonimapCardUseAddr, Long> {
    
    @Query("SELECT A.chamMonimapCardUseImageUrl FROM ChamMonimapCardUseAddr A WHERE A.chamMonimapCardUseAddrId = :cardUseAddrId")
    String findByImageUrl(@Param("cardUseAddrId") Long cardUseAddrId);
    
}
