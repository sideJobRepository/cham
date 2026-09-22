package com.cham.role.repository.query;

import com.cham.role.entity.ChamMonimapRole;

public interface ChamMonimapRoleQueryRepository {
    
    ChamMonimapRole findByMemberRoleName(String chamMonimapRoleName);
}
