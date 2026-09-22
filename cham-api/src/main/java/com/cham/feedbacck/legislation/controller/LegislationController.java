package com.cham.feedbacck.legislation.controller;

import com.cham.feedbacck.legislation.dto.response.LegislationFullResponse;
import com.cham.feedbacck.legislation.dto.response.LegislationReplyOnlyResponse;
import com.cham.feedbacck.legislation.service.LegislationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

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
    public LegislationReplyOnlyResponse getAllFullLegislations(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        Long memberId = Optional.ofNullable(jwt)
                .map(token -> token.getClaim("id"))
                .map(Object::toString)
                .map(Long::valueOf)
                .orElse(null);
        return legislationService.getLegislationRepliesOnly(id,memberId);
    }
    
    @GetMapping("/legislation/count/{id}")
    public Long getCount(@PathVariable Long id) {
        return legislationService.getAllReplyCount(id);
    }
    
    
    @GetMapping("/legislation/search")
    public LegislationFullResponse searchLegislations(@RequestParam String keyword) {
        return legislationService.searchLegislations(keyword);
    }
}
