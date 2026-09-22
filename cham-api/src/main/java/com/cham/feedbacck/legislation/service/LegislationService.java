package com.cham.feedbacck.legislation.service;

import com.cham.feedbacck.legislation.dto.response.LegislationFullResponse;
import com.cham.feedbacck.legislation.dto.response.LegislationReplyOnlyResponse;

public interface LegislationService {
    
    
    LegislationFullResponse searchLegislations(String keyword);
    
    LegislationReplyOnlyResponse getLegislationRepliesOnly(Long legislationId, Long memberId);
    
    LegislationFullResponse getAllFullLegislations();
    
    Long getAllReplyCount(Long id);
}
