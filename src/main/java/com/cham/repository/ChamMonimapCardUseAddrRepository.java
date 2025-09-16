package com.cham.repository;

import com.cham.entity.ChamMonimapCardUseAddr;
import com.cham.repository.query.ChamMonimapCardUseAddrQueryRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChamMonimapCardUseAddrRepository extends JpaRepository<ChamMonimapCardUseAddr, Long> , ChamMonimapCardUseAddrQueryRepository {
    

}
