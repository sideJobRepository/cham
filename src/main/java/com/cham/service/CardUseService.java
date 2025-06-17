package com.cham.service;

import com.cham.controller.response.ApiResponse;
import org.springframework.web.multipart.MultipartFile;

public interface CardUseService {
    
    ApiResponse insertCardUse(MultipartFile multipartFile);
}
