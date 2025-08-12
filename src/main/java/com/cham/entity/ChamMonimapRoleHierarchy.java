package com.cham.entity;

import com.cham.entity.base.BaseData;
import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Table(name = "CHAM_MONIMAP_ROLE_HIERARCHY")
@Getter
public class ChamMonimapRoleHierarchy extends BaseData {
    
    
    // 자치연대 예산감시 권한 계층 ID
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CHAM_MONIMAP_ROLE_HIERARCHY_ID")
    private Long chamMonimapRoleHierarchyId;
    
    // 자치연대 예산감시 권한 이름
    @Column(name = "CHAM_MONIMAP_ROLE_NAME")
    private String chamMonimapRoleName;
    
    // 자치연대 예산감시 상위 권한 ID
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CHAM_MONIMAP_PARENT_ROLE_ID")
    private ChamMonimapRoleHierarchy parent;
}
