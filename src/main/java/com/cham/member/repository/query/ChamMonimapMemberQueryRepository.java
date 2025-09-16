package com.cham.member.repository.query;

import com.cham.member.entity.ChamMonimapMember;

import java.util.Optional;

public interface ChamMonimapMemberQueryRepository {
    
    Optional<ChamMonimapMember> findByChamMonimapMemberSubId(String subId);
}
