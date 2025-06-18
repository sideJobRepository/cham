package com.cham.service.impl;

import com.cham.config.S3FileUtils;
import com.cham.controller.request.CardUseAddrImageRequest;
import com.cham.controller.response.ApiResponse;
import com.cham.entity.CardUseAddr;
import com.cham.repository.CardUseAddrRepository;
import com.cham.service.CardUseAddrService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;


@RequiredArgsConstructor
@Service
@Transactional
public class CardUseAddrServiceImpl implements CardUseAddrService {
    
    private final CardUseAddrRepository cardUseAddrRepository;
    
    private final S3FileUtils s3FileUtils;
    
    @Override
    public CardUseAddr insertCardUseAddr(CardUseAddr cardUseAddr) {
        return cardUseAddrRepository.save(cardUseAddr);
    }
    
    @Override
    public ApiResponse UpdateCardUseAddrImage(CardUseAddrImageRequest request) {
        
        MultipartFile cardUseImageUrl = request.getCardUseImageUrl();
        String s3Url = s3FileUtils.storeFile(cardUseImageUrl);
        
        CardUseAddr cardUseAddr = cardUseAddrRepository
                .findById(request.getCardUseAddrId())
                .orElseThrow(() -> new RuntimeException("해당 장소는 존재하지 않습니다."));
        cardUseAddr.updateImage(s3Url);
        return new ApiResponse(200,true,"이미지 업로드가 성공하였습니다.");
    }
}
