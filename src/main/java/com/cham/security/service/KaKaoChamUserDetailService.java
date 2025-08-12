package com.cham.security.service;

import com.cham.entity.ChamMonimapMember;
import com.cham.entity.enumeration.Role;
import com.cham.entity.enumeration.SocialType;
import com.cham.repository.ChamMonimapMemberRepository;
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
public class KaKaoChamUserDetailService implements UserDetailsService {
    
    private final ChamMonimapMemberRepository memberRepository;
    
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        
        return null;
    }
    
    public UserDetails loadUserByUsername(KaKaoProfileResponse kaKaoProfile) {
        ChamMonimapMember member = memberRepository.findBychamMonimapMemberSubId(String.valueOf(kaKaoProfile.getId()))
                .map(existingMember -> {
                    String thumbnailImageUrl = kaKaoProfile.getKakaoAccount().getProfile().getThumbnailImageUrl();
                    existingMember.modifyMemberImageUrl(thumbnailImageUrl);
                    return existingMember;
                })
                .orElseGet(() -> {
                    ChamMonimapMember newMember = toMember(kaKaoProfile);
                    return memberRepository.save(newMember);
                });
        List<GrantedAuthority> authorities =
                AuthorityUtils.createAuthorityList(member.getRole().name());
        
        return new ChamMonimapMemberContext(member, authorities);
    }
    
    private ChamMonimapMember toMember(KaKaoProfileResponse profile) {
        return ChamMonimapMember.builder()
                .chamMonimapMemberEmail(profile.getKakaoAccount().getEmail())
                .chamMonimapMemberName(profile.getKakaoAccount().getProfile().getNickname())
                .socialType(SocialType.KAKAO)
                .chamMonimapMemberSubId(String.valueOf(profile.getId()))
                .chamMonimapMemberImageUrl(profile.getKakaoAccount().getProfile().getThumbnailImageUrl())
                .role(Role.USER)          // 신규 가입 시 기본 권한
                .build();
    }
}
