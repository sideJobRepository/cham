package com.cham.memberrole.service;

import com.cham.page.PageResponse;
import com.cham.role.dto.MemberRoleGetResponse;
import org.springframework.data.domain.Pageable;

public interface ChamMonimapMemberRoleService {
    
    PageResponse<MemberRoleGetResponse> findByMemberRoles(Pageable pageable);
}
