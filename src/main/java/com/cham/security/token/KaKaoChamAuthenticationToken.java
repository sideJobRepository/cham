package com.cham.security.token;

import lombok.Getter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public class KaKaoChamAuthenticationToken extends AbstractAuthenticationToken {
    
    private final Object principal;
    
    private final Object credentials;
    
    @Getter
    private final String profileImageUrl;
    
    @Getter
    private final String thumbnailImageUrl;
    
    
    
    public KaKaoChamAuthenticationToken(Object principal, Object credentials, String profileImageUrl , String thumbnailImageUrl) {
        super(null);
        this.principal = principal;
        this.credentials = credentials;
        this.profileImageUrl = profileImageUrl;
        this.thumbnailImageUrl = thumbnailImageUrl;
        setAuthenticated(false);
    }
    
    public KaKaoChamAuthenticationToken(Object principal, Object credentials, String profileImageUrl , String thumbnailImageUrl,Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.principal = principal;
        this.credentials = credentials;
        this.profileImageUrl = profileImageUrl;
        this.thumbnailImageUrl = thumbnailImageUrl;
        setAuthenticated(true);
    }
    

    
    @Override
    public Object getCredentials() {
        return this.credentials;
    }
    
    @Override
    public Object getPrincipal() {
        return this.principal;
    }
}
