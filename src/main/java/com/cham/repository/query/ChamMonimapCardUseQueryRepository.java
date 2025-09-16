package com.cham.repository.query;

public interface ChamMonimapCardUseQueryRepository {
    
    boolean existsByChamMonimapCardUseDelkey(String cardUseDelkey);
    void deleteByCardUseDelkey(String cardUseDelkey);
}
