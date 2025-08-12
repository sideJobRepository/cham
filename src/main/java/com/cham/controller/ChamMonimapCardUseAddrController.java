package com.cham.controller;

import com.cham.controller.request.CardUseAddrImageRequest;
import com.cham.controller.response.ApiResponse;
import com.cham.service.ChamMonimapCardUseAddrService;
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
