package com.cham.feedbacck.great.dto.response;

import com.cham.feedbacck.great.enums.GreatType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class GreatTypeCount {
    private Long articleId;
    private GreatType greatType;
    private Long count;
}
