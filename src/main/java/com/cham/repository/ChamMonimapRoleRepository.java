package com.cham.repository;

import com.cham.entity.ChamMonimapRole;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChamMonimapRoleRepository extends JpaRepository<ChamMonimapRole, Long> {
    
    ChamMonimapRole findByChamMonimapRoleName(String chamMonimapRoleName);
}
