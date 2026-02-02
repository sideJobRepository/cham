package com.cham.feedbacck.great.dto.response;

import com.cham.feedbacck.great.enums.GreatType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class GreatMyTypeProjection {
    private Long greatId;
    private Long articleId;
     private GreatType greatType;
 
     public GreatMyTypeProjection(Long articleId, GreatType greatType) {
         this.articleId = articleId;
         this.greatType = greatType;
     }
}
