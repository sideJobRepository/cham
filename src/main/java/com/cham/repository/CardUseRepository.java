package com.cham.repository;

import com.cham.entity.CardOwnerPosition;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CardUseRepository extends JpaRepository<CardOwnerPosition, Long> {
}
