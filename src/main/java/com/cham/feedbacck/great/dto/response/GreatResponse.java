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
    
    private Long greatId;
    
    private Long articleId;
    
    private GreatType selectedType;
    
    /** 집계 */
    private Long supportCount;
    private Long oppositionCount;
    private Long concernCount;
    

}
