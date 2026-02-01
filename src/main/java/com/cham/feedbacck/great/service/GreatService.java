package com.cham.feedbacck.great.service;

import com.cham.dto.response.ApiResponse;
import com.cham.feedbacck.great.dto.request.GreatPostRequest;
import com.cham.feedbacck.great.dto.request.GreatPutRequest;
import com.cham.feedbacck.great.dto.response.GreatResponse;

import java.util.List;

public interface GreatService {
    
    
    GreatResponse getGreats(Long articleId, Long memberId);
    ApiResponse createGreat(GreatPostRequest request);
    ApiResponse updateGreat(GreatPutRequest request);
    
}
