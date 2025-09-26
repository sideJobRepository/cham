package com.cham.check.controller;


import com.cham.check.dto.request.CheckLogSuspiciousPostRequest;
import com.cham.check.dto.request.CheckLogVisitedPostRequest;
import com.cham.check.dto.response.CheckLogGetResponse;
import com.cham.check.service.ChamMonimapCheckLogService;
import com.cham.dto.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cham")
@RequiredArgsConstructor
public class ChamMonimapCheckLogController {

    private final ChamMonimapCheckLogService chamMonimapCheckLogService;

    @GetMapping("/check")
    public CheckLogGetResponse getCheckLog(@RequestParam(name = "addrId") Long addrId) {
        Authentication authentication = SecurityContextHolder.getContextHolderStrategy().getContext().getAuthentication();
        if (authentication instanceof AnonymousAuthenticationToken) {
            return chamMonimapCheckLogService.findByCheckAggregation(addrId, null);
        }
        JwtAuthenticationToken jwtAuthenticationToken = (JwtAuthenticationToken) authentication;
        Jwt token = jwtAuthenticationToken.getToken();
        Long id = token.getClaim("id");
        return chamMonimapCheckLogService.findByCheckAggregation(addrId, id);
    }

    @PostMapping("/check-visited")
    public ApiResponse createVisitedLog(@RequestBody CheckLogVisitedPostRequest request) {
        return chamMonimapCheckLogService.createCheckLogVisit(request);
    }

    @PostMapping("/check-suspicious")
    public ApiResponse createSuspicious(@RequestBody CheckLogSuspiciousPostRequest request) {
        return chamMonimapCheckLogService.createCheckLogSuspicious(request);
    }
}
