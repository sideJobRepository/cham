package com.cham.check.controller;


import com.cham.check.dto.CheckLogGetResponse;
import com.cham.check.service.ChamMonimapCheckLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/cham")
@RequiredArgsConstructor
public class ChamMonimapCheckLogController {

    private final ChamMonimapCheckLogService chamMonimapCheckLogService;
    
    @GetMapping("/check")
    public CheckLogGetResponse getCheckLog(@RequestParam(name = "addrId") Long addrId) {
        return chamMonimapCheckLogService.findByCheckAggregation(addrId);
    }
}
