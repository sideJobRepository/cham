package com.cham.role.repository;

import com.cham.role.entity.ChamMonimapRole;
import com.cham.role.repository.query.ChamMonimapRoleQueryRepository;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChamMonimapRoleRepository extends JpaRepository<ChamMonimapRole, Long> , ChamMonimapRoleQueryRepository {
    
}
