package com.cham.service;

import com.cham.controller.request.CardUseAddrImageRequest;
import com.cham.controller.response.ApiResponse;
import com.cham.entity.ChamMonimapCardUseAddr;


public interface ChamMonimapCardUseAddrService {
    
    ChamMonimapCardUseAddr insertCardUseAddr(ChamMonimapCardUseAddr cardUseAddr);
    
    ApiResponse UpdateCardUseAddrImage(CardUseAddrImageRequest request);
}
