package com.cham.theme.dto.request;

import com.cham.annotation.EnumValid;
import com.cham.theme.enumeration.ChamMonimapThemeType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class ThemePutRequest {
    
    @NotNull(message = "테마를 선택해 주세요")
    private Long themeId;
    
    private Long targetId;
    
    @NotBlank(message = "색깔은 필수 입니다.")
    private String  color;
    
    private String inputValue;
    
    @EnumValid(enumClass = ChamMonimapThemeType.class , allowNull = false, message = "유효하지 않은 테마 타입입니다.")
    private ChamMonimapThemeType type;
}
