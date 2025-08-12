package com.cham.service.impl;

import com.cham.entity.ChamMonimapCardOwnerPosition;
import com.cham.repository.ChamMonimapCardOwnerPositionRepository;
import com.cham.service.ChamMonimapCardOwnerPositionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
@Transactional
public class ChamMonimapCardOwnerPositionServiceImpl implements ChamMonimapCardOwnerPositionService {
    
    private final ChamMonimapCardOwnerPositionRepository cardOwnerPositionRepository;
    
    @Override
    @Transactional(readOnly = true)
    public List<ChamMonimapCardOwnerPosition> selectCardOwnerPosition() {
        return cardOwnerPositionRepository.findAll();
    }
}
