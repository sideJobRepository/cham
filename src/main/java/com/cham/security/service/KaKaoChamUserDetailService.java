package com.cham.security.service;

import com.cham.entity.Member;
import com.cham.entity.enumeration.Role;
import com.cham.entity.enumeration.SocialType;
import com.cham.repository.MemberRepository;
import com.cham.security.context.UserServiceContext;
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
import java.util.Optional;


@Service
@RequiredArgsConstructor
@Transactional
public class KaKaoChamUserDetailService implements UserDetailsService {
    
    private final MemberRepository memberRepository;
    
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        
        return null;
    }
    
    public UserDetails loadUserByUsername(KaKaoProfileResponse kaKaoProfile) {
        Member member = memberRepository.findByMemberSubId(String.valueOf(kaKaoProfile.getId()))
                .map(existingMember -> {
                    String thumbnailImageUrl = kaKaoProfile.getKakaoAccount().getProfile().getThumbnailImageUrl();
                    existingMember.modifyMemberImageUrl(thumbnailImageUrl);
                    return existingMember;
                })
                .orElseGet(() -> {
                    Member newMember = toMember(kaKaoProfile);
                    return memberRepository.save(newMember);
                });
        List<GrantedAuthority> authorities =
                AuthorityUtils.createAuthorityList(member.getRole().name());
        
        return new UserServiceContext(member, authorities);
    }
    
    private Member toMember(KaKaoProfileResponse profile) {
        return Member.builder()
                .memberEmail(profile.getKakaoAccount().getEmail())
                .memberName(profile.getKakaoAccount().getProfile().getNickname())
                .socialType(SocialType.KAKAO)
                .memberSubId(String.valueOf(profile.getId()))
                .memberImageUrl(profile.getKakaoAccount().getProfile().getThumbnailImageUrl())
                .role(Role.USER)          // 신규 가입 시 기본 권한
                .build();
    }
}
