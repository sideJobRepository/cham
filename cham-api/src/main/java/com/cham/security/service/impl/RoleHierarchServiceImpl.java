package com.cham.security.service.impl;

import com.cham.rolehierarchy.entity.ChamMonimapRoleHierarchy;
import com.cham.rolehierarchy.repository.ChamMonimapRoleHierarchyRepository;
import com.cham.security.service.ChamMonimapRoleHierarchyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class RoleHierarchServiceImpl implements ChamMonimapRoleHierarchyService {
    
    private final ChamMonimapRoleHierarchyRepository chamMonimapRoleHierarchyRepository;
    
    @Override
    public String findAllHierarchy() {
        List<ChamMonimapRoleHierarchy> roleHierarchiesList = chamMonimapRoleHierarchyRepository.findAll();
        StringBuilder hierarchy = new StringBuilder();
        
        for (ChamMonimapRoleHierarchy relation : roleHierarchiesList) {
            ChamMonimapRoleHierarchy parent = relation.getParent();
            if (parent == null) continue; // 부모가 없으면 스킵
            hierarchy.append("ROLE_")
                    .append(relation.getParent() != null ? relation.getParent().getChamMonimapRoleName() : relation.getChamMonimapRoleName())
                    .append(" > ROLE_")
                    .append(relation.getChamMonimapRoleName())
                    .append("\n");
        }
        
        return hierarchy.toString();
    }
}
