package com.cham.repository;

import com.cham.entity.ChamMonimapCardOwnerPosition;
import com.cham.repository.query.ChamMonimapCardOwnerPositionQueryRepository;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChamMonimapCardOwnerPositionRepository extends JpaRepository<ChamMonimapCardOwnerPosition, Long>, ChamMonimapCardOwnerPositionQueryRepository {

}
