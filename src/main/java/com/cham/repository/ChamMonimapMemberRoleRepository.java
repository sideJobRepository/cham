package com.cham.repository;

import com.cham.entity.ChamMonimapMemberRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ChamMonimapMemberRoleRepository extends JpaRepository<ChamMonimapMemberRole, Long> {
    
    Optional<ChamMonimapMemberRole> findByChamMonimapMember_ChamMonimapMemberId(Long memberId);
}
