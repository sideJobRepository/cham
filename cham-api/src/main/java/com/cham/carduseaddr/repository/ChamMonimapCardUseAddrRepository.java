package com.cham.carduseaddr.repository;

import com.cham.carduseaddr.entity.ChamMonimapCardUseAddr;
import com.cham.carduseaddr.repository.query.ChamMonimapCardUseAddrQueryRepository;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChamMonimapCardUseAddrRepository extends JpaRepository<ChamMonimapCardUseAddr, Long> , ChamMonimapCardUseAddrQueryRepository {
    

}
