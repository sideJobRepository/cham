package com.cham.caruse.service;

import com.cham.caruse.CardUseInsertOptions;
import com.cham.caruse.CardUseRow;
import com.cham.caruse.dto.CardUseAggregateResponse;
import com.cham.caruse.dto.CardUseUploadResponse;
import com.cham.caruse.dto.CleanupAddrResponse;
import com.cham.caruse.dto.CleanupNameResponse;
import com.cham.dto.request.CardUseConditionRequest;
import com.cham.dto.request.CardUseUploadDeleteKeyRequest;
import com.cham.dto.request.CardUseUploadPublicRequest;
import com.cham.dto.request.CleanupAddrRequest;
import com.cham.dto.request.CleanupNameRequest;
import com.cham.page.PageResponse;
import org.springframework.data.domain.Pageable;
import com.cham.dto.response.ApiResponse;
import com.cham.dto.response.CardUseResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface ChamMonimapCardUseService {
    
    CardUseAggregateResponse selectCardUse(CardUseConditionRequest request);
    
    CardUseAggregateResponse selectCardUseDetail(String request);
    
    
    ApiResponse insertCardUse(MultipartFile multipartFile);

    /**
     * 파싱이 끝난 줄을 저장한다. 수동 업로드와 수집 반영이 같이 쓴다.
     * 자리값(CardUseDefaults) 채우기, 직위 찾기, 주소 좌표 찾기가 여기서 일어난다.
     * @return 저장한 줄 수
     */
    int insertRows(List<CardUseRow> rows, String deleteKey, CardUseInsertOptions options);

    ApiResponse deleteExcel(String deleteKey);

    // 관리자 공개관리: 업로드(삭제키) 목록
    List<CardUseUploadResponse> selectCardUseUploads();

    // 관리자 공개관리: 업로드 한 건을 통째로 공개/비공개 전환
    ApiResponse modifyCardUseUploadPublic(CardUseUploadPublicRequest request);

    // 관리자 공개관리: 삭제키 이름 변경
    ApiResponse modifyCardUseUploadDeleteKey(CardUseUploadDeleteKeyRequest request);

    // 관리자 공개관리: 업로드 한 건을 업로드용 엑셀과 같은 형식으로 만들어 준다
    byte[] exportCardUseUpload(String deleteKey);

    // 자료정리: 손봐야 할 장소 목록 (keyword 가 있으면 그 이름으로 검색)
    PageResponse<CleanupAddrResponse> selectCleanupAddrs(String keyword, boolean onlyIssues, Pageable pageable);

    // 자료정리: 장소명·상세주소 수정. 주소가 바뀌면 좌표를 다시 찾는다
    ApiResponse modifyCleanupAddr(CleanupAddrRequest request);

    // 자료정리: 손봐야 할 이름 목록 (keyword 가 있으면 그 이름으로 검색)
    PageResponse<CleanupNameResponse> selectCleanupNames(String keyword, boolean onlyIssues, Pageable pageable);

    // 자료정리: 같은 표기를 쓰는 행의 이름을 한 번에 변경
    ApiResponse modifyCleanupName(CleanupNameRequest request);
}
