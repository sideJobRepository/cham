package com.cham.feedbacck.great.dto.request;

import com.cham.feedbacck.great.enums.GreatType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GreatPostRequest {
    
    private Long articleId;
    private Long memberId;
    private GreatType greatType;
}
