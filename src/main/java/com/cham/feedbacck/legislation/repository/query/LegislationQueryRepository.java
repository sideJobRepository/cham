package com.cham.feedbacck.legislation.repository.query;

import com.cham.feedbacck.legislationarticle.entity.LegislationArticle;

import java.util.List;

public interface LegislationQueryRepository  {

    
    List<LegislationArticle> searchArticlesByKeyword(String keyword);
    
}
