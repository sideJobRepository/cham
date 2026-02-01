package com.cham.feedbacck.great.dto.response;

import com.cham.feedbacck.great.enums.GreatType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class GreatResponse {
    
    /** 조문 ID */
    private Long articleId;
    
    /** 집계 */
    private Long supportCount;
    private Long oppositionCount;
    private Long concernCount;
    
    /** 로그인 사용자가 선택한 타입 (없으면 null) */
    private GreatType selectedType;
}
