package com.cham.repository;

import com.cham.entity.CardUse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CardUseRepository extends JpaRepository<CardUse, Long> {

    boolean existsByCardUseDelkey(String cardUseDelkey);
    
    @Modifying
    @Query("DELETE FROM CardUse c WHERE c.cardUseDelkey = :cardUseDelkey")
    void deleteByCardUseDelkey(@Param("cardUseDelkey") String cardUseDelkey);
}

