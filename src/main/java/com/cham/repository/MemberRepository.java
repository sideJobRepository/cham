package com.cham.repository;

import com.cham.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
public interface MemberRepository extends JpaRepository<Member,Long> {

    Member findByMemberSubId(String subId);
}
