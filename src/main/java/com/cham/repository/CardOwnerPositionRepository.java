package com.cham.repository;

import com.cham.entity.CardOwnerPosition;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CardOwnerPositionRepository extends JpaRepository<CardOwnerPosition, Long> {
    
    Optional<CardOwnerPosition> findByCardOwnerPositionName(String name);
}
