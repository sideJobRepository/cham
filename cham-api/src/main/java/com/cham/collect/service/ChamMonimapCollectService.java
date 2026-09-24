package com.cham.collect.service;

import com.cham.collect.dto.*;
import com.cham.collect.enumeration.CollectFileStatus;
import com.cham.dto.response.ApiResponse;
import com.cham.page.PageResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ChamMonimapCollectService {

    // 수집 출처(기관) 목록
    List<CollectSourceResponse> selectSources();

    // 월 표: 기관 × 1~12월 수집 상태
    CollectMonthMatrixResponse selectMonthMatrix(int year);

    PageResponse<CollectFileResponse> selectFiles(Long sourceId, Integer year, Integer month,
                                                  CollectFileStatus status, Pageable pageable);

    // 원본을 헤더 이름으로 읽어 반영될 모양을 보여준다. DB 에는 아무것도 쓰지 않는다
    CollectPreviewResponse preview(Long fileId, int limit, String defaultName);

    // 원본을 비공개 지도 자료로 넣는다. 공개는 공개관리 탭에서 켠다
    ApiResponse importFile(Long fileId, CollectImportRequest request, Long memberId);

    ApiResponse ignoreFile(Long fileId);

    // 무시/실패/반영을 검수 대기로 되돌린다. 반영한 파일은 그 삭제키를 먼저 지워야 한다
    ApiResponse resetFile(Long fileId);

    DownloadFile download(Long fileId);

    // 원본을 수동 업로드(/cham/upload) 양식 14열로 바꿔 준다. 손본 뒤 관리자 → 추가로 올리면 된다
    DownloadFile uploadForm(Long fileId, String defaultName);

    PageResponse<CollectJobResponse> selectJobs(Pageable pageable);

    // '수집' 버튼. 수집기가 1분 안에 가져간다
    ApiResponse requestJob(CollectJobRequest request, Long memberId);

    record DownloadFile(String fileName, byte[] body) {
    }
}
