package com.cham.repository;

import com.cham.entity.ChamMonimapCardUse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChamMonimapCardUseRepository extends JpaRepository<ChamMonimapCardUse, Long> {

    boolean existsByChamMonimapCardUseDelkey(String cardUseDelkey);
    
    @Modifying
    @Query("DELETE FROM ChamMonimapCardUse c WHERE c.chamMonimapCardUseDelkey = :cardUseDelkey")
    void deleteByCardUseDelkey(@Param("cardUseDelkey") String cardUseDelkey);
    
}

