package com.cham.repository;

import com.cham.entity.CardUseAddr;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CardUseAddrRepository extends JpaRepository<CardUseAddr, Long> {
    
    @Query("SELECT A.cardUseImageUrl FROM CardUseAddr A WHERE A.cardUseAddrId = :cardUseAddrId")
    String findByImageUrl(@Param("cardUseAddrId") Long cardUseAddrId);
    
}
