package com.cham.service.impl;

import com.cham.entity.CardOwnerPosition;
import com.cham.repository.CardOwnerPositionRepository;
import com.cham.service.CardOwnerPositionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
@Transactional
public class CardOwnerPositionServiceImpl implements CardOwnerPositionService {
    
    private final CardOwnerPositionRepository cardOwnerPositionRepository;
    
    @Override
    public List<CardOwnerPosition> selectCardOwnerPosition() {
        return cardOwnerPositionRepository.findAll();
    }
}
