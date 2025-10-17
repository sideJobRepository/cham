package com.cham.memberrole.service.impl;

import com.cham.RepositoryAndServiceTestSupport;
import com.cham.memberrole.dto.ChamMemberRoleGetResponse;
import com.cham.memberrole.service.ChamMonimapMemberRoleService;
import com.cham.page.PageResponse;
import com.cham.role.dto.MemberRoleGetResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

class ChamMonimapMemberRoleServiceImplTest extends RepositoryAndServiceTestSupport {
    
    @Autowired
    private ChamMonimapMemberRoleService chamMonimapMemberRoleService;
    
    
    @DisplayName("")
    @Test
    void test(){
        
        
        PageRequest pageRequest = PageRequest.of(0, 10);
        
        PageResponse<ChamMemberRoleGetResponse> byMemberRoles = chamMonimapMemberRoleService.findByMemberRoles(pageRequest);
        
        System.out.println("byMemberRoles = " + byMemberRoles);
        //
//        assertThat(byMemberRoles.content())
//                .extracting("memberId","memberRoleId","memberRoleName")
//                .contains(
//                        tuple(1L,1L,"USER")
//                );
        
    }
    
}