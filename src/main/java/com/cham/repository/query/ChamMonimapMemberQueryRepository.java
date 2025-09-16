package com.cham.repository.query;

import com.cham.entity.ChamMonimapMember;

import java.util.Optional;

public interface ChamMonimapMemberQueryRepository {
    
    Optional<ChamMonimapMember> findByChamMonimapMemberSubId(String subId);
}
