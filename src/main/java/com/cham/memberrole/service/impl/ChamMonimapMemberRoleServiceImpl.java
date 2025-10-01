package com.cham.memberrole.service.impl;

import com.cham.dto.response.ApiResponse;
import com.cham.member.repository.ChamMonimapMemberRepository;
import com.cham.memberrole.entity.ChamMonimapMemberRole;
import com.cham.memberrole.repository.ChamMonimapMemberRoleRepository;
import com.cham.memberrole.service.ChamMonimapMemberRoleService;
import com.cham.page.PageResponse;
import com.cham.role.dto.MemberRoleGetResponse;
import com.cham.role.dto.MemberRolePutRequest;
import com.cham.role.entity.ChamMonimapRole;
import com.cham.role.repository.ChamMonimapRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
@Service
@RequiredArgsConstructor
public class ChamMonimapMemberRoleServiceImpl implements ChamMonimapMemberRoleService {
    
    private final ChamMonimapMemberRoleRepository memberRoleRepository;
    
    private final ChamMonimapRoleRepository roleRepository;
    
    @Override
    public PageResponse<MemberRoleGetResponse> findByMemberRoles(Pageable pageable) {
        Page<ChamMonimapMemberRole> byMemberRoles = memberRoleRepository.findByMemberRoles(pageable);
        
        Page<MemberRoleGetResponse> result = byMemberRoles
                .map(item -> new MemberRoleGetResponse(
                        item.getChamMonimapMember().getChamMonimapMemberId(),
                        item.getChamMonimapRole().getChamMonimapRoleId(),
                        item.getChamMonimapMemberRoleId(),
                        item.getChamMonimapMember().getChamMonimapMemberName(),
                        item.getChamMonimapMember().getChamMonimapMemberEmail(),
                        item.getChamMonimapRole().getChamMonimapRoleName(),
                        item.getChamMonimapMember().getChamMonimapMemberPhoneNo()
                ));
        return PageResponse.from(result);
    }
    
    @Override
    public ApiResponse modifyMemberRole(List<MemberRolePutRequest> requests) {
        if(requests == null || requests.isEmpty()){
            return null;
        }
        for (MemberRolePutRequest request : requests) {
            Long roleId = request.getRoleId();
            Long memberRoleId = request.getMemberRoleId();
            
            ChamMonimapRole role = roleRepository.findById(roleId).orElseThrow(() -> new RuntimeException("존재 하지 않는 권한 입니다."));
            
            ChamMonimapMemberRole chamMonimapMemberRole = memberRoleRepository.findById(memberRoleId).orElseThrow(() -> new RuntimeException("존재 하지 않는 회원 입니다."));
            chamMonimapMemberRole.modifyRole(role);
        }
        return new ApiResponse(200, true, "권한이 변경되었습니다.");
    }
}
