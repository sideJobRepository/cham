package com.cham.caruse.repository.query;

import com.cham.caruse.entity.ChamMonimapCardUse;
import com.cham.caruse.repository.dto.CardUseSummaryDto;
import com.cham.dto.request.CardUseConditionRequest;

import java.util.List;

public interface ChamMonimapCardUseQueryRepository {
    
    boolean existsByChamMonimapCardUseDelkey(String cardUseDelkey);
    void deleteByCardUseDelkey(String cardUseDelkey);
    
    List<ChamMonimapCardUse> findByCardUses(CardUseConditionRequest cardUseConditionRequest);
    
    List<ChamMonimapCardUse> findByCardUsesDetail(String cardUsesDetail);
    
    List<CardUseSummaryDto> findBySumTotalAmount();
}
