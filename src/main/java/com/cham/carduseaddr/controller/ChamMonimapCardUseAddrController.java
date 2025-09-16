package com.cham.carduseaddr.controller;

import com.cham.dto.request.CardUseAddrImageRequest;
import com.cham.dto.response.ApiResponse;
import com.cham.carduseaddr.service.ChamMonimapCardUseAddrService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cham")
@RequiredArgsConstructor
public class ChamMonimapCardUseAddrController {
    
    private final ChamMonimapCardUseAddrService cardUseAddrService;
    
    
    @PostMapping("/cardUseAddrImage")
    public ApiResponse modifyAddrImage(@ModelAttribute CardUseAddrImageRequest request) {
        return cardUseAddrService.UpdateCardUseAddrImage(request);
    }
}
