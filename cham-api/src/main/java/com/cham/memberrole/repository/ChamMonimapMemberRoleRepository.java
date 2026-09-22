package com.cham.memberrole.repository;

import com.cham.memberrole.entity.ChamMonimapMemberRole;
import com.cham.memberrole.repository.query.ChamMonimapMemberRoleQueryRepository;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChamMonimapMemberRoleRepository extends JpaRepository<ChamMonimapMemberRole, Long> , ChamMonimapMemberRoleQueryRepository {
    
}
