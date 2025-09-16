package com.cham.member.repository;

import com.cham.member.entity.ChamMonimapMember;
import com.cham.member.repository.query.ChamMonimapMemberQueryRepository;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChamMonimapMemberRepository extends JpaRepository<ChamMonimapMember,Long> , ChamMonimapMemberQueryRepository {
    
}
