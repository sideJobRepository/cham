package com.cham.controller;


import com.cham.controller.response.CardOwnerPositionResponse;
import com.cham.entity.ChamMonimapCardOwnerPosition;
import com.cham.service.ChamMonimapCardOwnerPositionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequestMapping("/cham")
@RequiredArgsConstructor
@RestController
public class ChamMonimapCardOwnerPositionController {

    private final ChamMonimapCardOwnerPositionService cardOwnerPositionService;
    
    @GetMapping("/position")
    public List<CardOwnerPositionResponse> findCardOwnerPosition() {
        List<ChamMonimapCardOwnerPosition> cardOwnerPositions = cardOwnerPositionService.selectCardOwnerPosition();
        return cardOwnerPositions.stream().map(item -> new CardOwnerPositionResponse(item.getChamMonimapCardOwnerPositionId(), item.getChamMonimapCardOwnerPositionName())).toList();
    }
}
