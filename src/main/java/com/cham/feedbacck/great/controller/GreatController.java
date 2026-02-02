package com.cham.feedbacck.great.controller;

import com.cham.dto.response.ApiResponse;
import com.cham.feedbacck.great.dto.request.GreatPostRequest;
import com.cham.feedbacck.great.dto.request.GreatPutRequest;
import com.cham.feedbacck.great.dto.response.GreatResponse;
import com.cham.feedbacck.great.service.GreatService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RequiredArgsConstructor
@RestController
@RequestMapping("/cham")
public class GreatController {
    
    private final GreatService greatService;
    
    @GetMapping("/great/{articleId}")
    public GreatResponse getGreat(@PathVariable Long articleId, @AuthenticationPrincipal Jwt jwt) {
        Long memberId = Optional.ofNullable(jwt)
                .map(token -> token.getClaim("id"))
                .map(Object::toString)
                .map(Long::valueOf)
                .orElse(null);
        return greatService.getGreats(articleId, memberId);
    }
    
    @PostMapping("/great")
    public ApiResponse createGreat(@RequestBody GreatPostRequest request, @AuthenticationPrincipal Jwt jwt) {
        Long memberId = Optional.ofNullable(jwt)
                .map(token -> token.getClaim("id"))
                .map(Object::toString)
                .map(Long::valueOf)
                .orElse(null);
        request.setMemberId(memberId);
        return greatService.createGreat(request);
    }
    
    @PutMapping("/great")
    public ApiResponse modifyGreat(@RequestBody GreatPutRequest request) {
        return greatService.updateGreat(request);
    }
}
