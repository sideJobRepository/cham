package com.cham.theme.enumeration;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum ChamMonimapThemeType {
    OWNER("직위"),
    INPUT("직적입력");
    
    private final String value;
}
