package com.cham.feedbacck.legislation.controller;

import com.cham.feedbacck.legislation.dto.response.LegislationFullResponse;
import com.cham.feedbacck.legislation.dto.response.LegislationReplyOnlyResponse;
import com.cham.feedbacck.legislation.service.LegislationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cham")
@RequiredArgsConstructor
public class LegislationController {
    
    private final LegislationService legislationService;
    
    
    @GetMapping("/legislation")
    public LegislationFullResponse getAllFullLegislations() {
        return legislationService.getAllFullLegislations();
    }
    
    @GetMapping("/legislation/{id}")
    public LegislationReplyOnlyResponse getAllFullLegislations(@PathVariable Long id) {
        return legislationService.getLegislationRepliesOnly(id);
    }
    
    
    @GetMapping("/legislation/search")
    public LegislationFullResponse searchLegislations(@RequestParam String keyword) {
        return legislationService.searchLegislations(keyword);
    }
}
