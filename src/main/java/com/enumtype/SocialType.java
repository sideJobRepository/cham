package com.enumtype;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum SocialType {
    
    KAKAO("카카오"),
    NAVER("네이버"),
    GOOGLE("구글");
    
    
    private final String name;
  
    
}
