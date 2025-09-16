package com.cham.carduseaddr.service;

import com.cham.carduseaddr.entity.ChamMonimapCardUseAddr;
import com.cham.dto.request.CardUseAddrImageRequest;
import com.cham.dto.response.ApiResponse;


public interface ChamMonimapCardUseAddrService {
    
    ChamMonimapCardUseAddr insertCardUseAddr(ChamMonimapCardUseAddr cardUseAddr);
    
    ApiResponse UpdateCardUseAddrImage(CardUseAddrImageRequest request);
}
