package com.cham.security.service;

import com.cham.entity.*;
import com.cham.entity.enumeration.SocialType;
import com.cham.repository.ChamMonimapMemberRepository;
import com.cham.repository.ChamMonimapMemberRoleRepository;
import com.cham.repository.ChamMonimapRoleRepository;
import com.cham.security.context.ChamMonimapMemberContext;
import com.cham.security.service.impl.response.KaKaoProfileResponse;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.cham.entity.QChamMonimapMember.*;
import static com.cham.entity.QChamMonimapMemberRole.*;
import static com.cham.entity.QChamMonimapRole.*;


@Service
@RequiredArgsConstructor
@Transactional
public class KaKaoChamUserDetailService implements UserDetailsService {
    
    private final ChamMonimapMemberRepository chamMonimapMemberRepository;
    
    private final ChamMonimapRoleRepository chamMonimapRoleRepository;
    
    private final ChamMonimapMemberRoleRepository chamMonimapMemberRoleRepository;
    
    private final JPAQueryFactory queryFactory;
    
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        
        return null;
    }
    
    public UserDetails loadUserByUsername(KaKaoProfileResponse kaKaoProfile) {
        ChamMonimapMember findChamMoniMapMember = chamMonimapMemberRepository.findByChamMonimapMemberSubId(String.valueOf(kaKaoProfile.getId()))
                .orElseGet(() -> {
                    ChamMonimapMember agitMember = new ChamMonimapMember(kaKaoProfile);
                    ChamMonimapMember saveMember = chamMonimapMemberRepository.save(agitMember);
                    
                    ChamMonimapRole findChamMonimapRole = chamMonimapRoleRepository.findByChamMonimapRoleName("USER");
                    
                    ChamMonimapMemberRole chamMonimapMemberRole = new ChamMonimapMemberRole(saveMember, findChamMonimapRole);
                    
                    chamMonimapMemberRoleRepository.save(chamMonimapMemberRole);
                    return saveMember;
                });
        
        
        List<String> roleNames = queryFactory
                .select(chamMonimapRole.chamMonimapRoleName)
                .from(chamMonimapMember)
                .join(chamMonimapMemberRole).on(chamMonimapMember.eq(chamMonimapMemberRole.chamMonimapMember))
                .join(chamMonimapRole).on(chamMonimapRole.eq(chamMonimapMemberRole.chamMonimapRole))
                .where(chamMonimapMember.chamMonimapMemberId.eq(findChamMoniMapMember.getChamMonimapMemberId()))
                .fetch();
        
        List<GrantedAuthority> authorityList = AuthorityUtils.createAuthorityList(roleNames);
        
        return new ChamMonimapMemberContext(findChamMoniMapMember, authorityList);
    }
    
}
