package com.cham.memberrole.controller;


import com.cham.memberrole.service.ChamMonimapMemberRoleService;
import com.cham.page.PageResponse;
import com.cham.role.dto.MemberRoleGetResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/cham")
public class ChamMonimapMemberRoleController {
    
    private final ChamMonimapMemberRoleService chamMonimapMemberRoleService;
    
    @GetMapping("/role")
    public PageResponse<MemberRoleGetResponse> chamMemberGetRole(Pageable pageable) {
        return chamMonimapMemberRoleService.findByMemberRoles(pageable);
    }
}
