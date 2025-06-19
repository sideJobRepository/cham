package com.cham.entity;

import com.cham.entity.base.BaseData;
import com.cham.entity.enumeration.Role;
import com.cham.entity.enumeration.SocialType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Table(name = "MEMBER")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Member extends BaseData {
    // 회원 ID
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MEMBER_ID")
    private Long memberId;
    
    // 회원 이메일
    @Column(name = "MEMBER_EMAIL")
    private String memberEmail;
    
    // 회원 이름
    @Column(name = "MEMBER_NAME")
    private String memberName;
    
    // 회원 소셜 타입
    @Column(name = "MEMBER_SOCIAL_TYPE")
    @Enumerated(EnumType.STRING)
    private SocialType socialType;
    
    @Column(name = "MEMBER_SUB_ID")
    private String memberSubId;
    
    @Column(name = "MEMBER_ROLE")
    @Enumerated(EnumType.STRING)
    private Role role;
    
}
