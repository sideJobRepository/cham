package com.cham.repository;

import com.cham.entity.CardUse;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CardUseRepository extends JpaRepository<CardUse, Long> {

    boolean existsByCardUseDelkey(String cardUseDelkey);
}

