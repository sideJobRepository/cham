package com.cham.memberrole.service;

import com.cham.dto.response.ApiResponse;
import com.cham.page.PageResponse;
import com.cham.role.dto.MemberRoleGetResponse;
import com.cham.role.dto.MemberRolePutRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ChamMonimapMemberRoleService {
    
    PageResponse<MemberRoleGetResponse> findByMemberRoles(Pageable pageable);
    
    ApiResponse modifyMemberRole(List<MemberRolePutRequest> requests);
}
