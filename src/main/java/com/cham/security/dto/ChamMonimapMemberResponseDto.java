package com.cham.security.dto;

import com.cham.entity.ChamMonimapMember;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class ChamMonimapMemberResponseDto {
    
    
    private Long id;
    private String name;
    private List<String> roles;
    private String socialId;
    private String sub;
    private String phoneNumber;
    
    public static ChamMonimapMemberResponseDto create(ChamMonimapMember member, List<GrantedAuthority> authorities) {
        ChamMonimapMemberResponseDto bgmAgitMemberResponseDto = new ChamMonimapMemberResponseDto();
        bgmAgitMemberResponseDto.setId(member.getChamMonimapMemberId());
        bgmAgitMemberResponseDto.setName(member.getChamMonimapMemberName());
        List<String> roleList = new ArrayList<>();
        if (authorities != null && !authorities.isEmpty()) {
            for (GrantedAuthority auth : authorities) {
                roleList.add("ROLE_" + auth.getAuthority());
            }
        }
        bgmAgitMemberResponseDto.setRoles(roleList);
        bgmAgitMemberResponseDto.setSocialId(member.getChamMonimapMemberSubId());
        //bgmAgitMemberResponseDto.setPhoneNumber(member.getBgmAgitMemberPhoneNo());
        bgmAgitMemberResponseDto.setPhoneNumber("");
        bgmAgitMemberResponseDto.setSub("user");
        return bgmAgitMemberResponseDto;
    }
}
