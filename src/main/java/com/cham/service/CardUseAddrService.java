package com.cham.service;

import com.cham.controller.response.ApiResponse;
import com.cham.entity.CardUseAddr;
import org.springframework.web.multipart.MultipartFile;


public interface CardUseAddrService {
    
    CardUseAddr insertCardUseAddr(CardUseAddr cardUseAddr);
}
