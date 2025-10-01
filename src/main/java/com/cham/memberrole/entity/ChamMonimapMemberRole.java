package com.cham.memberrole.entity;


import com.base.BaseData;
import com.cham.member.entity.ChamMonimapMember;
import com.cham.role.entity.ChamMonimapRole;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "CHAM_MONIMAP_MEMBER_ROLE")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChamMonimapMemberRole extends BaseData {
    
    
    // 자치연대 예산감시 회원 권한 ID
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CHAM_MONIMAP_MEMBER_ROLE_ID")
    private Long chamMonimapMemberRoleId;
    
    // 자치연대 예산감시 회원 ID
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CHAM_MONIMAP_MEMBER_ID")
    private ChamMonimapMember chamMonimapMember;
    
    // 자치연대 예산감시 권한 ID
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CHAM_MONIMAP_ROLE_ID")
    private ChamMonimapRole chamMonimapRole;
    
    public ChamMonimapMemberRole(ChamMonimapMember chamMonimapMember, ChamMonimapRole chamMonimapRole) {
        this.chamMonimapMember = chamMonimapMember;
        this.chamMonimapRole = chamMonimapRole;
    }
    
    public void modifyRole(ChamMonimapRole role) {
        this.chamMonimapRole = role;
    }
}
