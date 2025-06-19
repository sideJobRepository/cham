package com.cham.repository;

import com.cham.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member,Long> {
    
    Optional<Member> findByMemberSubId(String subId);
}
