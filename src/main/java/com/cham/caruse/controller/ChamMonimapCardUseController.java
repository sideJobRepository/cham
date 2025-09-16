package com.cham.caruse.controller;


import com.cham.dto.request.CardUseConditionRequest;
import com.cham.dto.response.CardUseResponse;
import com.cham.caruse.service.ChamMonimapCardUseService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/cham")
@RequiredArgsConstructor
public class ChamMonimapCardUseController {
    
    private final ChamMonimapCardUseService cardUseService;
    
    @PostMapping("/cardUse")
    public Map<Long, CardUseResponse> getCardUse(@RequestBody CardUseConditionRequest request) {
        return cardUseService.selectCardUse(request);
    }
}
