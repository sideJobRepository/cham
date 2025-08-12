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
    private String nickname;
    private List<String> roles;
    private String socialId;
    private String sub;
    private String phoneNumber;
    private String imageUrl;
    
    public static ChamMonimapMemberResponseDto create(ChamMonimapMember member, List<GrantedAuthority> authorities) {
        ChamMonimapMemberResponseDto dto = new ChamMonimapMemberResponseDto();
        dto.setId(member.getChamMonimapMemberId());
        dto.setName(member.getChamMonimapMemberName());
        dto.setNickname(member.getChamMonimapMemberNickname());
        List<String> roleList = new ArrayList<>();
        if (authorities != null && !authorities.isEmpty()) {
            for (GrantedAuthority auth : authorities) {
                roleList.add("ROLE_" + auth.getAuthority());
            }
        }
        dto.setRoles(roleList);
        dto.setSocialId(member.getChamMonimapMemberSubId());
        dto.setPhoneNumber(member.getChamMonimapMemberPhoneNo());
        dto.setImageUrl(member.getChamMonimapMemberImageUrl());
        dto.setSub("user");
        return dto;
    }
}
