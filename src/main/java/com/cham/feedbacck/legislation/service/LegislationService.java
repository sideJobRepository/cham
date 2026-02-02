package com.cham.feedbacck.legislation.service;

import com.cham.feedbacck.legislation.dto.response.LegislationFullResponse;

public interface LegislationService {

    LegislationFullResponse getAllFullLegislations();
    
    LegislationFullResponse searchLegislations(String keyword);
}
