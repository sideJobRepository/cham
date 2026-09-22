package com.cham.feedbacck.great.repository;

import com.cham.feedbacck.great.entity.Great;
import com.cham.feedbacck.great.repository.query.GreatQueryRepository;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GreatRepository extends JpaRepository<Great,Long>, GreatQueryRepository {

}
