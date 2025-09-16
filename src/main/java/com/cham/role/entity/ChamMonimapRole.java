package com.cham.role.entity;

import com.base.BaseData;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "CHAM_MONIMAP_ROLE")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChamMonimapRole extends BaseData {
    
    
    // 자치연대 예산감시 권한 ID
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CHAM_MONIMAP_ROLE_ID")
    private Long chamMonimapRoleId;
    
    // 자치연대 예산감시 권한 이름
    @Column(name = "CHAM_MONIMAP_ROLE_NAME")
    private String chamMonimapRoleName;
}
