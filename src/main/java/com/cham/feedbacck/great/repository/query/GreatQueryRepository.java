package com.cham.feedbacck.great.repository.query;

import com.cham.feedbacck.great.dto.response.GreatTypeCount;
import com.cham.feedbacck.great.enums.GreatType;

import java.util.List;

public interface GreatQueryRepository {
    List<GreatTypeCount> findGreatCounts(Long articleId);
    GreatType findMyGreatType(Long articleId, Long memberId);
}
