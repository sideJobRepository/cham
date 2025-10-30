package com.cham.theme.dto.response;

import com.cham.theme.enumeration.ChamMonimapThemeType;
import com.querydsl.core.annotations.QueryProjection;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ThemeGetResponse {
    
    private Long themeId;
    
    private Long targetId;
    
    private String color;
    
    private String themeType;
    
    private String inputValue;
    
    private String positionName;
    
    @QueryProjection
    public ThemeGetResponse(Long themeId, Long targetId, String color, String themeType, String inputValue, String positionName) {
        this.themeId = themeId;
        this.targetId = targetId;
        this.color = color;
        this.themeType = themeType;
        this.inputValue = inputValue;
        this.positionName = positionName;
    }
}
