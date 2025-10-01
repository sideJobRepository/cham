package com.cham.memberrole.controller;


import com.cham.dto.response.ApiResponse;
import com.cham.memberrole.service.ChamMonimapMemberRoleService;
import com.cham.page.PageResponse;
import com.cham.role.dto.MemberRoleGetResponse;
import com.cham.role.dto.MemberRolePutRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/cham")
public class ChamMonimapMemberRoleController {
    
    // 어드민 권한만 조회및 수정 가능
    private final ChamMonimapMemberRoleService chamMonimapMemberRoleService;
    
    @GetMapping("/role")
    public PageResponse<MemberRoleGetResponse> chamMemberGetRole(Pageable pageable) {
        return chamMonimapMemberRoleService.findByMemberRoles(pageable);
    }
    @PutMapping("/role")
    public ApiResponse chamMemberPutRole(@Validated @RequestBody List<MemberRolePutRequest> requests) {
        return chamMonimapMemberRoleService.modifyMemberRole(requests);
    }
}
