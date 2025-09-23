package com.cham.check.controller;


import com.cham.check.dto.request.CheckLogSuspiciousPostRequest;
import com.cham.check.dto.request.CheckLogVisitedPostRequest;
import com.cham.check.dto.response.CheckLogGetResponse;
import com.cham.check.service.ChamMonimapCheckLogService;
import com.cham.dto.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cham")
@RequiredArgsConstructor
public class ChamMonimapCheckLogController {

    private final ChamMonimapCheckLogService chamMonimapCheckLogService;
    
    @GetMapping("/check")
    public CheckLogGetResponse getCheckLog(@RequestParam(name = "addrId") Long addrId , @RequestParam(name = "memberId",required = false) Long memberId) {
        return chamMonimapCheckLogService.findByCheckAggregation(addrId, memberId);
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
