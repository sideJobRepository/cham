package com.cham.caruse.service;

import com.cham.dto.request.CardUseConditionRequest;
import com.cham.dto.response.ApiResponse;
import com.cham.dto.response.CardUseResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

public interface ChamMonimapCardUseService {
    
    Map<Long, CardUseResponse> selectCardUse(CardUseConditionRequest request);
    
    Map<Long, CardUseResponse> selectCardUseDetail(String request);
    
    
    ApiResponse insertCardUse(MultipartFile multipartFile);
    
    ApiResponse deleteExcel(String deleteKey);
}
