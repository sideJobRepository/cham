package com.cham.theme.dto.response;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@NoArgsConstructor
public class ThemeGetResponse {
    
    private Long themeId;
    
    private Long targetId;
    
    private String color;
    
    private String themeType;
    
    private String inputValue;
    
    private String positionName;
    
    private String fileUrl;
    
    private MultipartFile file;
    
    @QueryProjection
    
    public ThemeGetResponse(Long themeId, Long targetId, String color, String themeType, String inputValue, String positionName, String fileUrl) {
        this.themeId = themeId;
        this.targetId = targetId;
        this.color = color;
        this.themeType = themeType;
        this.inputValue = inputValue;
        this.positionName = positionName;
        this.fileUrl = fileUrl;
    }
}
