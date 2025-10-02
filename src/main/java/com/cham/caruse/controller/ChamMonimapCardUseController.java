package com.cham.caruse.controller;


import com.cham.caruse.dto.CardUseAggregateResponse;
import com.cham.caruse.service.ChamMonimapCardUseService;
import com.cham.dto.request.CardUseConditionRequest;
import com.cham.dto.response.CardUseResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/cham")
@RequiredArgsConstructor
public class ChamMonimapCardUseController {
    
    private final ChamMonimapCardUseService cardUseService;
    
    
    @GetMapping("/cardUse")
    public CardUseAggregateResponse getCardUse(
            @RequestParam(name = "cardOwnerPositionId",required = false) Long cardOwnerPositionId ,
            @RequestParam(name = "input",required = false) String input) {
        return cardUseService.selectCardUse(new CardUseConditionRequest(cardOwnerPositionId, input));
    }
    
    @GetMapping("/cardUseDetail")
    public CardUseAggregateResponse getCardUseDetail(@RequestParam(name = "addrDetail") String addrDetail) {
        return cardUseService.selectCardUseDetail(addrDetail);
    }
    
}
