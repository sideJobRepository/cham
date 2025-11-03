package com.cham.theme.enumeration;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum ChamMonimapFlag {
    C("생성"),
    P("수정");
    private final String value;
}
