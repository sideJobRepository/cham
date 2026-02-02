package com.cham.feedbacck.legislationarticle.repository;

import com.cham.feedbacck.legislation.entity.Legislation;
import com.cham.feedbacck.legislationarticle.entity.LegislationArticle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LegislationArticleRepository extends JpaRepository<LegislationArticle,Long> {

    List<LegislationArticle> findByLegislationOrderByOrdersNo(Legislation legislation);
    
    
}
