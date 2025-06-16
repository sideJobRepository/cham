package com.cham.repository;


import com.cham.entity.CardOwner;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CardOwnerRepository  extends JpaRepository<CardOwner, Long> {

}
