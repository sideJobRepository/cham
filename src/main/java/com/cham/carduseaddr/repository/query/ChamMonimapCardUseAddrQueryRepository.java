package com.cham.carduseaddr.repository.query;

import com.cham.carduseaddr.entity.ChamMonimapCardUseAddr;
import com.cham.dto.response.CardUseAddrDto;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface ChamMonimapCardUseAddrQueryRepository {
    String findByImageUrl(Long cardUseAddrId);
    
    List<CardUseAddrDto> findByCardUseAddrDtos();
    
    List<ChamMonimapCardUseAddr> findImageUrlsByAddrIds(Set<Long> addrIds);
    Optional<ChamMonimapCardUseAddr> findByXValueAndYValue(String x, String y,double range);
}
