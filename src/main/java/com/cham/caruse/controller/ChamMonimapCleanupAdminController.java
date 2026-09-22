package com.cham.caruse.controller;

import com.cham.caruse.dto.CleanupAddrResponse;
import com.cham.caruse.dto.CleanupNameResponse;
import com.cham.caruse.service.ChamMonimapCardUseService;
import com.cham.dto.request.CleanupAddrRequest;
import com.cham.dto.request.CleanupNameRequest;
import com.cham.dto.response.ApiResponse;
import com.cham.page.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 맛집지도 자료정리(관리자 전용).
 *
 * 엑셀에 '어딘지 모르겠어요' 처럼 메모가 적혀 들어온 값을 고치는 화면이다.
 * 권한은 /cham/admin/** 로 한 번에 걸려 있다.
 */
@RestController
@RequestMapping("/cham/admin/cleanup")
@RequiredArgsConstructor
public class ChamMonimapCleanupAdminController {

    private final ChamMonimapCardUseService cardUseService;

    /** 기본은 전체. onlyIssues=true 면 손봐야 할 것만 남긴다. */
    @GetMapping("/addrs")
    public PageResponse<CleanupAddrResponse> getCleanupAddrs(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "false") boolean onlyIssues,
            @PageableDefault(size = 20) Pageable pageable) {
        return cardUseService.selectCleanupAddrs(keyword, onlyIssues, pageable);
    }

    @PutMapping("/addr")
    public ApiResponse modifyCleanupAddr(@Validated @RequestBody CleanupAddrRequest request) {
        return cardUseService.modifyCleanupAddr(request);
    }

    /** 기본은 전체. onlyIssues=true 면 손봐야 할 것만 남긴다. */
    @GetMapping("/names")
    public PageResponse<CleanupNameResponse> getCleanupNames(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "false") boolean onlyIssues,
            @PageableDefault(size = 20) Pageable pageable) {
        return cardUseService.selectCleanupNames(keyword, onlyIssues, pageable);
    }

    @PutMapping("/name")
    public ApiResponse modifyCleanupName(@Validated @RequestBody CleanupNameRequest request) {
        return cardUseService.modifyCleanupName(request);
    }
}
