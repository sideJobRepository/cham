package com.cham.feedbacck.legislation.repository;

import com.cham.feedbacck.legislation.entity.Legislation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LegislationRepository extends JpaRepository<Legislation,Long> {
    Optional<Legislation> findByBillVersion(String billVersion);
}
