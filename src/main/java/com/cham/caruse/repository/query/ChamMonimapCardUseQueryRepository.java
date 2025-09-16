package com.cham.caruse.repository.query;

public interface ChamMonimapCardUseQueryRepository {
    
    boolean existsByChamMonimapCardUseDelkey(String cardUseDelkey);
    void deleteByCardUseDelkey(String cardUseDelkey);
}
