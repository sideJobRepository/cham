package com.cham.service.impl;

import com.cham.entity.CardUseAddr;
import com.cham.repository.CardUseAddrRepository;
import com.cham.service.CardUseAddrService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@RequiredArgsConstructor
@Service
@Transactional
public class CardUseAddrServiceImpl implements CardUseAddrService {
    
    private final CardUseAddrRepository cardUseAddrRepository;
    
    @Override
    public CardUseAddr insertCardUseAddr(CardUseAddr cardUseAddr) {
        return cardUseAddrRepository.save(cardUseAddr);
    }
}
