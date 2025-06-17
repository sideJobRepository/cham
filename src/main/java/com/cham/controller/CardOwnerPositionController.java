package com.cham.controller;


import com.cham.controller.response.CardOwnerPositionResponse;
import com.cham.entity.CardOwnerPosition;
import com.cham.service.CardOwnerPositionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequestMapping("/cham")
@RequiredArgsConstructor
@RestController
public class CardOwnerPositionController {

    private final CardOwnerPositionService cardOwnerPositionService;
    
    @GetMapping("/position")
    public List<CardOwnerPositionResponse> findCardOwnerPosition() {
        List<CardOwnerPosition> cardOwnerPositions = cardOwnerPositionService.selectCardOwnerPosition();
        return cardOwnerPositions.stream().map(item -> new CardOwnerPositionResponse(item.getCardOwnerPositionId(), item.getCardOwnerPositionName())).toList();
    }
}
