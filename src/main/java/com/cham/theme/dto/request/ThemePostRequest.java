package com.cham.theme.dto.request;

import com.cham.annotation.EnumValid;
import com.cham.theme.enumeration.ChamMonimapFlag;
import com.cham.theme.enumeration.ChamMonimapThemeType;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class ThemePostRequest {
    
    private Long themeId;
    private Long targetId; // 직위 일때는 직위 ID 보내줘야함 , 직접입력은 안보내줘도됨
    @NotBlank(message = "색깔은 필수 입니다.")
    private String color; // 색깔
    private String inputValue;
    @EnumValid(enumClass = ChamMonimapThemeType.class , allowNull = false, message = "유효하지 않은 테마 타입입니다.")
    private ChamMonimapThemeType type; // 직위 일때는 : OWNER ,  직접입력은 : INPUT 으로 보내주면됨
    @EnumValid(enumClass = ChamMonimapFlag.class, allowNull = false,message = "플래그 값을 입력해주세요")
    private ChamMonimapFlag flag;
    
    private MultipartFile file;
    
}
