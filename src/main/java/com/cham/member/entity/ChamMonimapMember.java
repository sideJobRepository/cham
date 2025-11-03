package com.cham.member.entity;

import com.cham.base.BaseData;
import com.cham.security.service.impl.response.SocialProfile;
import com.enumtype.SocialType;
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
    
    // 자치연대 예산감시 회원 닉네임
    @Column(name = "CHAM_MONIMAP_MEMBER_NICKNAME")
    private String chamMonimapMemberNickname;
    
    // 회원 소셜 타입
    @Column(name = "CHAM_MONIMAP_MEMBER_SOCIAL_TYPE")
    @Enumerated(EnumType.STRING)
    private SocialType socialType;
    // 자치연대 예산감시 회원 전화 번호
    @Column(name = "CHAM_MONIMAP_MEMBER_PHONE_NO")
    private String chamMonimapMemberPhoneNo;
    
    // 자치연대 예산감시 회원 SUB ID
    @Column(name = "CHAM_MONIMAP_MEMBER_SUB_ID")
    private String chamMonimapMemberSubId;
    // 자치연대 예산감시 회원 이미지 URL
    @Column(name = "CHAM_MONIMAP_MEMBER_IMAGE_URL")
    private String chamMonimapMemberImageUrl;
    
    public ChamMonimapMember(SocialProfile profile) {
        this.chamMonimapMemberEmail = profile.email();
        this.chamMonimapMemberName = profile.name();
        this.socialType = profile.provider();
        this.chamMonimapMemberSubId = String.valueOf(profile.sub());
        this.chamMonimapMemberPhoneNo = profile.phone();
        this.chamMonimapMemberImageUrl = profile.profileImageUrl();
        this.chamMonimapMemberNickname = profile.nickname();
    }
    
    public ChamMonimapMember(Long chamMonimapMemberId) {
        this.chamMonimapMemberId = chamMonimapMemberId;
    }
    
    public void modifyMemberImageUrl(String memberImageUrl) {
        this.chamMonimapMemberImageUrl = memberImageUrl;
    }
    
}
