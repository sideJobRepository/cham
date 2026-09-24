package com.cham.collect.repository.query;

import com.cham.collect.dto.CollectFileCell;
import com.cham.collect.dto.CollectFileResponse;
import com.cham.collect.enumeration.CollectFileStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ChamMonimapCollectFileQueryRepository {

    // 수집관리 파일 목록. 조건은 모두 선택. 최근에 받은 것부터
    Page<CollectFileResponse> findFiles(Long sourceId, Integer year, Integer month, CollectFileStatus status, Pageable pageable);

    // 월 표를 만들 파일들. 중복(DUPLICATE)은 뺀다
    List<CollectFileCell> findYearCells(int year);
}
