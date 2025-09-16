package com.cham.role.repository;

import com.cham.role.entity.ChamMonimapRole;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChamMonimapRoleRepository extends JpaRepository<ChamMonimapRole, Long> {
    
    ChamMonimapRole findByChamMonimapRoleName(String chamMonimapRoleName);
}
