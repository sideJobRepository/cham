package com.cham.feedbacck.legislation.repository;

import com.cham.feedbacck.legislation.entity.Legislation;
import com.cham.feedbacck.legislation.repository.query.LegislationQueryRepository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LegislationRepository extends JpaRepository<Legislation,Long>, LegislationQueryRepository {
    Optional<Legislation> findByBillVersion(String billVersion);
    
    Optional<Legislation> findFirstByOrderByIdAsc();
}
