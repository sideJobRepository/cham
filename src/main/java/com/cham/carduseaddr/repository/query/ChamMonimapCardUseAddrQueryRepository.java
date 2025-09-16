package com.cham.carduseaddr.repository.query;

import org.springframework.data.repository.query.Param;

public interface ChamMonimapCardUseAddrQueryRepository {
    String findByImageUrl(@Param("cardUseAddrId") Long cardUseAddrId);
}
