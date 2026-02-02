package com.cham.feedbacck.great.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class GreatGetPostRequest {
    
    private List<Long> articleIds;
}
