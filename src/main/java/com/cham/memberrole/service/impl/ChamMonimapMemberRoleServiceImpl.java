package com.cham.memberrole.service.impl;

import com.cham.memberrole.entity.ChamMonimapMemberRole;
import com.cham.memberrole.repository.ChamMonimapMemberRoleRepository;
import com.cham.memberrole.service.ChamMonimapMemberRoleService;
import com.cham.page.PageResponse;
import com.cham.role.dto.MemberRoleGetResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Service
@RequiredArgsConstructor
public class ChamMonimapMemberRoleServiceImpl implements ChamMonimapMemberRoleService {
    
    private final ChamMonimapMemberRoleRepository chamMonimapMemberRoleRepository;
    
    @Override
    public PageResponse<MemberRoleGetResponse> findByMemberRoles(Pageable pageable) {
        Page<ChamMonimapMemberRole> byMemberRoles = chamMonimapMemberRoleRepository.findByMemberRoles(pageable);
        
        Page<MemberRoleGetResponse> result = byMemberRoles
                .map(item -> new MemberRoleGetResponse(
                        item.getChamMonimapMember().getChamMonimapMemberId(),
                        item.getChamMonimapMemberRoleId(),
                        item.getChamMonimapMember().getChamMonimapMemberName(),
                        item.getChamMonimapMember().getChamMonimapMemberEmail(),
                        item.getChamMonimapRole().getChamMonimapRoleName()));
        return PageResponse.from(result);
    }
}
