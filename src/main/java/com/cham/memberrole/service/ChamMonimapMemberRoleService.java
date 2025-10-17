package com.cham.memberrole.service;

import com.cham.dto.response.ApiResponse;
import com.cham.memberrole.dto.ChamMemberRoleGetResponse;
import com.cham.page.PageResponse;
import com.cham.role.dto.MemberRolePutRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ChamMonimapMemberRoleService {
    
    PageResponse<ChamMemberRoleGetResponse> findByMemberRoles(Pageable pageable);
    
    ApiResponse modifyMemberRole(List<MemberRolePutRequest> requests);
}
