package com.cham.carduseaddr.repository.query;

import com.cham.dto.response.CardUseAddrDto;

import java.util.List;

public interface ChamMonimapCardUseAddrQueryRepository {
    String findByImageUrl(Long cardUseAddrId);
    
    List<CardUseAddrDto> findByCardUseAddrDtos();
}
