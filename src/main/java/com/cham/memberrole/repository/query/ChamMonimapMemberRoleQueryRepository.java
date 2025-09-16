package com.cham.memberrole.repository.query;

import com.cham.memberrole.entity.ChamMonimapMemberRole;

import java.util.List;
import java.util.Optional;

public interface ChamMonimapMemberRoleQueryRepository {
    
    Optional<ChamMonimapMemberRole> findByMemberRole(Long memberId);
    
    List<String> findByRoleName(Long id);
}
