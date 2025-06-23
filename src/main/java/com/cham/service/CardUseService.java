package com.cham.service;

import com.cham.controller.request.CardUseConditionRequest;
import com.cham.controller.response.ApiResponse;
import com.cham.controller.response.CardUseResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

public interface CardUseService {
    
    Map<Long, CardUseResponse> selectCardUse(CardUseConditionRequest request);
    
    ApiResponse insertCardUse(MultipartFile multipartFile);
    
    ApiResponse deleteExcel(String deleteKey);
}
