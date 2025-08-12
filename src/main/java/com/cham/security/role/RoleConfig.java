package com.cham.security.role;



import com.cham.service.ChamMonimapRoleHierarchyService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;

@Configuration
public class RoleConfig {


    @Bean
    public RoleHierarchyImpl roleHierarchy(ChamMonimapRoleHierarchyService chamMonimapRoleHierarchyService) {
        return RoleHierarchyImpl.fromHierarchy(chamMonimapRoleHierarchyService.findAllHierarchy());
    }
}
