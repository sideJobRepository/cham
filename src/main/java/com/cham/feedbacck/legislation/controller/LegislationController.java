package com.cham.feedbacck.legislation.controller;

import com.cham.feedbacck.legislation.dto.response.LegislationFullResponse;
import com.cham.feedbacck.legislation.service.LegislationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/cham")
@RequiredArgsConstructor
public class LegislationController {
    
    private final LegislationService legislationService;
    
    
    @GetMapping("/legislation")
    public LegislationFullResponse getAllFullLegislations() {
        return legislationService.getAllFullLegislations();
    }
    
    @GetMapping("/legislation/search")
    public LegislationFullResponse searchLegislations(@RequestParam String keyword) {
        return legislationService.searchLegislations(keyword);
    }
}
