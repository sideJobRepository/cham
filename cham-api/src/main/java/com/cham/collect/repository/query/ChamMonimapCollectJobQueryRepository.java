package com.cham.collect.repository.query;

import com.cham.collect.dto.CollectJobResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ChamMonimapCollectJobQueryRepository {

    // 대기(REQUESTED) 또는 실행 중(RUNNING)인 작업이 있는지
    boolean existsActive();

    // 작업 이력. 최근 것부터. 요청자 이름과 기관명을 붙인다
    Page<CollectJobResponse> findJobs(Pageable pageable);
}
