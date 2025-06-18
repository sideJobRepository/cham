package com.cham.security.service;

import com.cham.entity.Member;
import com.cham.entity.enumeration.SocialType;
import com.cham.repository.MemberRepository;
import com.cham.security.service.impl.response.KaKaoProfileResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
@Transactional
public class KaKaoChamUserDetailService implements UserDetailsService {
    
    private final MemberRepository memberRepository;
    
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        
        return null;
    }
    
    public void loadUserByUsername(KaKaoProfileResponse kaKaoProfile) {
        String subId = String.valueOf(kaKaoProfile.getId());
        Member findMember = memberRepository.findByMemberSubId(subId);
        
        
        
        
        new Member(kaKaoProfile.getKakao_account().getEmail(), kaKaoProfile.getKakao_account().getNickname(), SocialType.KAKAO, subId);
    }
}
