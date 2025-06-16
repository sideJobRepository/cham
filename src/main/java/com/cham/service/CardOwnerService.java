package com.cham.service;


import com.cham.controller.response.ApiResponse;
import com.cham.entity.CardOwner;
import org.springframework.web.multipart.MultipartFile;

public interface CardOwnerService {

    
    ApiResponse insertCardOwner(MultipartFile multipartFile);
}
