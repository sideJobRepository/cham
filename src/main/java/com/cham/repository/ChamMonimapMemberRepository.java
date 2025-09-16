package com.cham.repository;

import com.cham.entity.ChamMonimapMember;
import com.cham.repository.query.ChamMonimapMemberQueryRepository;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChamMonimapMemberRepository extends JpaRepository<ChamMonimapMember,Long> , ChamMonimapMemberQueryRepository {
    
}
