package com.cham.repository;

import com.cham.entity.ChamMonimapMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ChamMonimapMemberRepository extends JpaRepository<ChamMonimapMember,Long> {
    
    Optional<ChamMonimapMember> findBychamMonimapMemberSubId(String subId);
}
