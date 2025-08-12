package com.cham.security.role;



import com.cham.security.role.response.RoleMapResponse;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.cham.entity.QChamMonimapRole.*;
import static com.cham.entity.QChamMonimapUrlResources.*;
import static com.cham.entity.QChamMonimapUrlResourcesRole.*;


@Service
@Transactional(readOnly = true)
public class ChamMonimapUrlRoleMapping {
    
    private LinkedHashMap<String, String> urlRoleMappings = new LinkedHashMap<>();
    private final JPAQueryFactory queryFactory;
    
    public ChamMonimapUrlRoleMapping(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }
    
    public Map<String,String> getRoleMappings() {
        urlRoleMappings.clear();
        
        List<RoleMapResponse> resourcesList = queryFactory
                .select(Projections.constructor(
                        RoleMapResponse.class,
                        chamMonimapUrlResources.chamMonimapUrlResourcesPath,
                        chamMonimapRole.chamMonimapRoleName,
                        chamMonimapUrlResources.chamMonimapHttpMethods
                ))
                .from(chamMonimapUrlResourcesRole)
                .join(chamMonimapUrlResourcesRole.chamMonimapUrlResources, chamMonimapUrlResources)
                .join(chamMonimapUrlResourcesRole.chamMonimapRole, chamMonimapRole)
                .fetch();

        resourcesList
                .forEach(resources -> {
                    String key = resources.getHttpMethod().toUpperCase() + " " + resources.getBgmAgitUrlResourcesPath(); // "POST /bgm-agit/notice"
                    urlRoleMappings.put(key, "ROLE_" + resources.getBgmAgitRoleName());
                });
        return urlRoleMappings;
 
    }
}
