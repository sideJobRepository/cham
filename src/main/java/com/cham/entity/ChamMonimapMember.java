package com.cham.entity;

import com.cham.entity.base.BaseData;
import com.cham.entity.enumeration.Role;
import com.cham.entity.enumeration.SocialType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicUpdate;

@Entity
@Getter
@Table(name = "CHAM_MONIMAP_MEMBER")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@DynamicUpdate
public class ChamMonimapMember extends BaseData {
    // 회원 ID
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CHAM_MONIMAP_MEMBER_ID")
    private Long chamMonimapMemberId;
    
    // 회원 이메일
    @Column(name = "CHAM_MONIMAP_MEMBER_EMAIL")
    private String chamMonimapMemberEmail;
    
    // 회원 이름
    @Column(name = "CHAM_MONIMAP_MEMBER_NAME")
    private String chamMonimapMemberName;
    
    // 회원 소셜 타입
    @Column(name = "CHAM_MONIMAP_MEMBER_SOCIAL_TYPE")
    @Enumerated(EnumType.STRING)
    private SocialType socialType;
    
    @Column(name = "CHAM_MONIMAP_MEMBER_SUB_ID")
    private String chamMonimapMemberSubId;
    
    @Column(name = "CHAM_MONIMAP_MEMBER_ROLE")
    @Enumerated(EnumType.STRING)
    private Role role;
    
    @Column(name = "CHAM_MONIMAP_MEMBER_IMAGE_URL")
    private String chamMonimapMemberImageUrl;
    
    public ChamMonimapMember(Long memberId) {
        this.chamMonimapMemberId = memberId;
    }
    
    
    public void modifyMemberImageUrl(String memberImageUrl) {
        this.chamMonimapMemberImageUrl = memberImageUrl;
    }
    
}
