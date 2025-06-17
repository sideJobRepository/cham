package com.cham.controller;


import com.cham.controller.request.CardUseConditionRequest;
import com.cham.controller.response.CardUseResponse;
import com.cham.service.CardUseService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/cham")
@RequiredArgsConstructor
public class CardUseController {
    
    private final CardUseService cardUseService;
    
    @PostMapping("/cardUse")
    public Map<Long, CardUseResponse> getCardUse(@RequestBody CardUseConditionRequest request) {
        return cardUseService.selectCardUse(request);
    }
}
