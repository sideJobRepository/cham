package com.cham.service.impl;

import com.cham.config.S3FileUtils;
import com.cham.controller.request.CardUseAddrImageRequest;
import com.cham.controller.response.ApiResponse;
import com.cham.entity.ChamMonimapCardUseAddr;
import com.cham.repository.ChamMonimapCardUseAddrRepository;
import com.cham.service.ChamMonimapCardUseAddrService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;


@RequiredArgsConstructor
@Service
@Transactional
public class ChamMonimapCardUseAddrServiceImpl implements ChamMonimapCardUseAddrService {
    
    private final ChamMonimapCardUseAddrRepository cardUseAddrRepository;
    
    private final S3FileUtils s3FileUtils;
    
    @Override
    public ChamMonimapCardUseAddr insertCardUseAddr(ChamMonimapCardUseAddr cardUseAddr) {
        return cardUseAddrRepository.save(cardUseAddr);
    }
    
    @Override
    public ApiResponse  UpdateCardUseAddrImage(CardUseAddrImageRequest request) {
        
        MultipartFile cardUseImageUrl = request.getCardUseImageUrl();
        String byCardUseImageUrl = cardUseAddrRepository.findByImageUrl(request.getCardUseAddrId());
        if(StringUtils.hasText(byCardUseImageUrl)) {
            s3FileUtils.deleteFile(byCardUseImageUrl);
        }
        String s3Url = s3FileUtils.storeFile(cardUseImageUrl);
        
        ChamMonimapCardUseAddr cardUseAddr = cardUseAddrRepository
                .findById(request.getCardUseAddrId())
                .orElseThrow(() -> new RuntimeException("해당 장소는 존재하지 않습니다."));
        cardUseAddr.updateImage(s3Url);
        return new ApiResponse(200,true,"이미지 업로드가 성공하였습니다.");
    }
}
