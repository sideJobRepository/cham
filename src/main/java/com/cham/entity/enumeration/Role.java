package com.cham.entity.enumeration;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum Role {
    
    USER("일반 회원"),
    ADMIN("관리자");
    
    private final String name;
}
