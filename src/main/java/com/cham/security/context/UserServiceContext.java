package com.cham.security.context;

import com.cham.entity.ChamMonimapMember;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Getter
@RequiredArgsConstructor
public class UserServiceContext implements UserDetails {
    
    private final ChamMonimapMember member;
    private final List<GrantedAuthority> authorities;
    
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return this.authorities;
    }
    
    @Override
    public String getPassword() {
        return "";
    }
    
    @Override
    public String getUsername() {
        return member.getChamMonimapMemberName();
    }
}
