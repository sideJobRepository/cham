package com.cham.security.service.impl;

import com.cham.member.entity.ChamMonimapMember;
import com.cham.member.repository.ChamMonimapMemberRepository;
import com.cham.memberrole.entity.ChamMonimapMemberRole;
import com.cham.memberrole.repository.ChamMonimapMemberRoleRepository;
import com.cham.role.entity.ChamMonimapRole;
import com.cham.role.repository.ChamMonimapRoleRepository;
import com.cham.security.context.ChamMonimapMemberContext;
import com.cham.security.service.impl.response.KaKaoProfileResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
@RequiredArgsConstructor
@Transactional
public class KaKaoChamUserDetailServiceImpl implements UserDetailsService {
    
    private final ChamMonimapMemberRepository chamMonimapMemberRepository;
    
    private final ChamMonimapRoleRepository chamMonimapRoleRepository;
    
    private final ChamMonimapMemberRoleRepository chamMonimapMemberRoleRepository;
    
    
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        
        return null;
    }
    
    public UserDetails loadUserByUsername(KaKaoProfileResponse kaKaoProfile) {
        ChamMonimapMember findChamMoniMapMember = chamMonimapMemberRepository.findByMemberSubId(String.valueOf(kaKaoProfile.getId()))
                .orElseGet(() -> {
                    ChamMonimapMember agitMember = new ChamMonimapMember(kaKaoProfile);
                    ChamMonimapMember saveMember = chamMonimapMemberRepository.save(agitMember);
                    
                    ChamMonimapRole findChamMonimapRole = chamMonimapRoleRepository.findByMemberRoleName("USER");
                    
                    ChamMonimapMemberRole chamMonimapMemberRole = new ChamMonimapMemberRole(saveMember, findChamMonimapRole);
                    
                    chamMonimapMemberRoleRepository.save(chamMonimapMemberRole);
                    return saveMember;
                });
        
        List<String> roleNames = chamMonimapMemberRoleRepository.findByRoleName(findChamMoniMapMember.getChamMonimapMemberId());
        
        List<GrantedAuthority> authorityList = AuthorityUtils.createAuthorityList(roleNames);
        
        return new ChamMonimapMemberContext(findChamMoniMapMember, authorityList);
    }
    
}
