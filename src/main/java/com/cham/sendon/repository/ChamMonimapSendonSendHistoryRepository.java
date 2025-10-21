package com.cham.sendon.repository;


import com.cham.sendon.entity.ChamMonimapSendonSendHistory;
import com.cham.sendon.repository.query.ChamMonimapSendonSendHistoryQueryRepository;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChamMonimapSendonSendHistoryRepository extends JpaRepository<ChamMonimapSendonSendHistory, Long> , ChamMonimapSendonSendHistoryQueryRepository {
}
