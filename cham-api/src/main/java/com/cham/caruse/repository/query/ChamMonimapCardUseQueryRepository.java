package com.cham.caruse.repository.query;

import com.cham.caruse.dto.CardUseUploadMemberResponse;
import com.cham.caruse.dto.CardUseUploadResponse;
import com.cham.caruse.dto.CleanupAddrResponse;
import com.cham.caruse.dto.CleanupNameResponse;
import com.cham.caruse.entity.ChamMonimapCardUse;
import com.cham.caruse.repository.dto.CardUseSummaryDto;
import com.cham.dto.request.CardUseConditionRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

public interface ChamMonimapCardUseQueryRepository {

    boolean existsByChamMonimapCardUseDelkey(String cardUseDelkey);
    void deleteByCardUseDelkey(String cardUseDelkey);

    List<ChamMonimapCardUse> findByCardUses(CardUseConditionRequest cardUseConditionRequest);

    List<ChamMonimapCardUse> findByCardUsesDetail(String cardUsesDetail);

    List<CardUseSummaryDto> findBySumTotalAmount();

    // 관리자 공개관리: 삭제키로 묶은 업로드 목록
    List<CardUseUploadResponse> findCardUseUploads();

    // 관리자 공개관리: 업로드별 사용자 명단. 목록 전체 분을 한 번에 받아 서비스에서 묶는다.
    List<CardUseUploadMemberResponse> findCardUseUploadMembers();

    // 관리자 공개관리: 삭제키 하나를 통째로 공개/비공개 전환. 바뀐 행 수를 돌려준다.
    long updatePublicByDelkey(String cardUseDelkey, boolean isPublic);

    // 관리자 공개관리: 삭제키 이름 변경. 그 업로드에 속한 행을 전부 바꾼다.
    long updateDelkey(String cardUseDelkey, String newCardUseDelkey);

    // 관리자 공개관리: 엑셀로 내려받을 원본 행. 공개 여부와 무관하게 전부 담는다.
    List<ChamMonimapCardUse> findByDelkeyForExport(String cardUseDelkey);

    // 자료정리: 장소 목록. 기본은 전부 보여주고 손봐야 할 것을 위로 올린다.
    // onlyIssues 가 true 면 손봐야 할 것만 남긴다.
    Page<CleanupAddrResponse> findCleanupAddrs(String keyword, boolean onlyIssues, Pageable pageable);

    // 자료정리: 이름 목록. 위와 같다.
    Page<CleanupNameResponse> findCleanupNames(String keyword, boolean onlyIssues, Pageable pageable);

    // 자료정리: 같은 표기를 쓰는 행의 이름을 한 번에 바꾼다.
    long updateCardUseName(String oldName, String newName);

    // 수집 반영: 한 지역에서 사용자(직함)별로 가장 최근에 쓴 이름. 의회 원본에는 사람 이름이 없어서
    // '의장' → '오은규' 처럼 기존 자료로 채운다.
    Map<String, String> findLatestNameByUser(String region);
}
