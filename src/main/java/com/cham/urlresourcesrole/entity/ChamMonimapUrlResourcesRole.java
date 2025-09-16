package com.cham.urlresourcesrole.entity;

import com.cham.role.entity.ChamMonimapRole;
import com.cham.urlresource.entity.ChamMonimapUrlResources;
import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Getter
@Table(name = "CHAM_MONIMAP_URL_RESOURCES_ROLE")
public class ChamMonimapUrlResourcesRole {
    
    // 자치연대 예산감시 URL  리소스 권한 ID
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CHAM_MONIMAP_URL_RESOURCES_ROLE_ID")
    private Long chamMonimapUrlResourcesRoleId;
    
    // 자치연대 예산감시 권한 ID
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CHAM_MONIMAP_ROLE_ID")
    private ChamMonimapRole chamMonimapRole;
    
    // 자치연대 예산감시 URL 리소스 ID
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CHAM_MONIMAP_URL_RESOURCES_ID")
    private ChamMonimapUrlResources chamMonimapUrlResources;
}
