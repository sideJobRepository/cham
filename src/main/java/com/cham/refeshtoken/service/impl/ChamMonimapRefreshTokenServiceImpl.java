package com.cham.refeshtoken.service.impl;

import com.cham.dto.response.ApiResponse;
import com.cham.member.entity.ChamMonimapMember;
import com.cham.refeshtoken.entity.ChamMonimapRefreshToken;
import com.cham.memberrole.repository.ChamMonimapMemberRoleRepository;
import com.cham.refeshtoken.repository.ChamMonimapRefreshTokenRepository;
import com.cham.security.dto.ChamMonimapMemberResponseDto;
import com.cham.security.dto.TokenAndUser;
import com.cham.security.handler.TokenPair;
import com.cham.security.jwt.RsaSecuritySigner;
import com.cham.refeshtoken.service.ChamMonimapRefreshTokenService;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.JWK;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ChamMonimapRefreshTokenServiceImpl implements ChamMonimapRefreshTokenService {
    
    private final ChamMonimapRefreshTokenRepository chamMonimapRefreshTokenRepository;
    private final ChamMonimapMemberRoleRepository chamMonimapMemberRoleRepository;
    private final RsaSecuritySigner rsaSecuritySigner;
    private final JWK jwk;
    
    @Override
    public void refreshTokenSaveOrUpdate(ChamMonimapMember member, String refreshTokenValue, LocalDateTime expiresAt) {
        
        ChamMonimapRefreshToken token = chamMonimapRefreshTokenRepository
                .findByMemberToken(member)
                .orElse(ChamMonimapRefreshToken.builder()
                        .chamMonimapMember(member)
                        .chamMonimapRefreshTokenValue(refreshTokenValue)
                        .chamMonimapTokenRefreshExpiresDate(expiresAt)
                        .build());
        
        if (token.getChamMonimapRefreshTokenId() != null) {
            token.updateToken(refreshTokenValue, expiresAt);
        }
        chamMonimapRefreshTokenRepository.save(token);
    }
    
    @Override
    public ChamMonimapMember validateRefreshToken(String refreshToken) {
        ChamMonimapRefreshToken token = chamMonimapRefreshTokenRepository
                .findByTokenValue(refreshToken)
                .orElseThrow(() -> new RuntimeException("유효하지 않은 리프레시 토큰입니다."));
        
        if (token.getChamMonimapTokenRefreshExpiresDate().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("리프레시 토큰이 만료되었습니다.");
        }
        
        return token.getChamMonimapMember(); // fetch join 필요시 수정
    }
    
    @Override
    public TokenAndUser reissueTokenWithUser(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            return null;
        }
        
        ChamMonimapMember member = validateRefreshToken(refreshToken);
        
        String roleName = chamMonimapMemberRoleRepository.findByMemberRole(member.getChamMonimapMemberId()).orElseThrow(() -> new RuntimeException("존재 하지 않는 권한"))
                .getChamMonimapRole()
                .getChamMonimapRoleName();
        
        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(roleName));
        
        try {
            TokenPair token = rsaSecuritySigner.getToken(member, jwk, authorities);
            
            refreshTokenSaveOrUpdate(
                    member,
                    token.getRefreshToken(),
                    LocalDateTime.now().plusDays(1)
            );
            
            // 로그인 때와 동일하게 DTO 생성
            ChamMonimapMemberResponseDto user = ChamMonimapMemberResponseDto.create(member, authorities);
            
            return new TokenAndUser(token, user);
        } catch (JOSEException e) {
            throw new RuntimeException("JWT 생성 실패", e);
        }
    }
    
    @Override
    public ApiResponse deleteRefresh(String refreshToken) {
        ChamMonimapRefreshToken chamMonimapRefreshToken = chamMonimapRefreshTokenRepository.findByTokenValue(refreshToken).orElseThrow(() -> new RuntimeException("존재하지않는 리프레쉬 토큰입니다."));
        chamMonimapRefreshTokenRepository.delete(chamMonimapRefreshToken);
        return new ApiResponse(200,true,"정상 삭제");
    }
}
