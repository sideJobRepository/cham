package com.cham.security.service.impl.response;

import com.enumtype.SocialType;


public record SocialProfile(
        SocialType provider,      // KAKAO / NAVER / GOOGLE
        String sub,               // 공급자 고유 ID
        String email,
        String name,
        String nickname,
        String phone,
        String profileImageUrl,
        String thumbnailImageUrl
) {}

