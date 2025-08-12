package com.cham.service.impl;

import com.cham.security.role.ChamMonimapUrlRoleMapping;
import com.cham.service.ChamMonimapDynamicAuthorizationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@Transactional
@RequiredArgsConstructor
public class ChamMonimapDynamicAuthorizationServiceImpl implements ChamMonimapDynamicAuthorizationService {
    
    
    private final ChamMonimapUrlRoleMapping chamMonimapUrlRoleMapping;
    
    
    @Override
    @Transactional(readOnly = true)
    public Map<String, String> getUrlRoleMappings() {
        return chamMonimapUrlRoleMapping.getRoleMappings();
    }
}
