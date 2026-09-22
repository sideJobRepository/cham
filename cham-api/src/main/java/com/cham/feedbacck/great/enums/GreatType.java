package com.cham.feedbacck.great.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum GreatType {
    
    OPPOSITION("반대"),
    SUPPORT("찬성"),
    CONCERN("우려");
    
    private final String value;
}
