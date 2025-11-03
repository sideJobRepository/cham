package com.cham.theme.entity;

import com.cham.base.BaseData;
import com.cham.theme.dto.request.ThemePutRequest;
import com.cham.theme.enumeration.ChamMonimapThemeType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "CHAM_MONIMAP_THEME")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ChamMonimapTheme extends BaseData {
    
    // 자치연대 예산감시 테마 ID
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CHAM_MONIMAP_THEME_ID")
    private Long id;
    
    // 자치연대 예산감시 타겟 ID
    @Column(name = "CHAM_MONIMAP_TARGET_ID")
    private Long targetId;
    
    // 자치연대 예산감시 테마 색깔
    @Column(name = "CHAM_MONIMAP_THEME_COLOR")
    private String color;
    
    // 자치연대 예산감시 타입
    @Column(name = "CHAM_MONIMAP_THEME_TYPE")
    @Enumerated(EnumType.STRING)
    private ChamMonimapThemeType type;
    
    @Column(name = "CHAM_MONIMAP_INPUT_VALUE")
    private String inputValue;
    
    public void modify(ThemePutRequest themePutRequest) {
        this.targetId = themePutRequest.getTargetId();
        this.color = themePutRequest.getColor();
        this.type = themePutRequest.getType();
        this.inputValue = themePutRequest.getInputValue();
    }
}
