package com.cham.carduseaddr.service;

import com.cham.dto.request.CardUseAddrImageRequest;
import com.cham.dto.response.ApiResponse;


public interface ChamMonimapCardUseAddrService {
    
    
    ApiResponse UpdateCardUseAddrImage(CardUseAddrImageRequest request);
}
