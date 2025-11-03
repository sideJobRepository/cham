package com.cham.file.enumeration;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum ChamMonimapFileType {
    THEME("테마");
    
    private final String value;
}
