package com.cham.collect.controller;

import com.cham.collect.dto.*;
import com.cham.collect.enumeration.CollectFileStatus;
import com.cham.collect.service.ChamMonimapCollectService;
import com.cham.dto.response.ApiResponse;
import com.cham.page.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.time.Year;
import java.util.List;
import java.util.Optional;

/**
 * 업무추진비 수집관리(관리자 전용).
 *
 * 수집기(cham-collector)가 기관 게시판에서 받아둔 원본을 확인하고 지도 자료로 반영한다.
 * 권한은 코드가 아니라 CHAM_MONIMAP_URL_RESOURCES 의 '/cham/admin/**' 행으로 건다
 * (cham-api/sql/collect-admin-url.sql). 행이 없으면 누구나 호출할 수 있으니 반드시 넣고 재기동할 것.
 */
@RestController
@RequestMapping("/cham/admin/collect")
@RequiredArgsConstructor
public class ChamMonimapCollectAdminController {

    private final ChamMonimapCollectService collectService;

    @GetMapping("/sources")
    public List<CollectSourceResponse> getSources() {
        return collectService.selectSources();
    }

    @GetMapping("/months")
    public CollectMonthMatrixResponse getMonths(@RequestParam(required = false) Integer year) {
        return collectService.selectMonthMatrix(year == null ? Year.now().getValue() : year);
    }

    @GetMapping("/files")
    public PageResponse<CollectFileResponse> getFiles(
            @RequestParam(required = false) Long sourceId,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) CollectFileStatus status,
            @PageableDefault(size = 20) Pageable pageable) {
        return collectService.selectFiles(sourceId, year, month, status, pageable);
    }

    @GetMapping("/files/{fileId}/preview")
    public CollectPreviewResponse getPreview(@PathVariable Long fileId,
                                             @RequestParam(defaultValue = "50") int limit,
                                             @RequestParam(required = false) String defaultName) {
        return collectService.preview(fileId, Math.min(limit, 500), defaultName);
    }

    @PostMapping("/files/{fileId}/import")
    public ApiResponse importFile(@PathVariable Long fileId,
                                  @RequestBody(required = false) CollectImportRequest request,
                                  @AuthenticationPrincipal Jwt jwt) {
        return collectService.importFile(fileId, request, memberId(jwt));
    }

    @PutMapping("/files/{fileId}/ignore")
    public ApiResponse ignoreFile(@PathVariable Long fileId) {
        return collectService.ignoreFile(fileId);
    }

    @PutMapping("/files/{fileId}/reset")
    public ApiResponse resetFile(@PathVariable Long fileId) {
        return collectService.resetFile(fileId);
    }

    @GetMapping("/files/{fileId}/download")
    public ResponseEntity<byte[]> download(@PathVariable Long fileId) {
        return attachment(collectService.download(fileId));
    }

    /**
     * PDF 미리보기. 수집기가 S3 에 올려 둔 원본을 그대로 내려주되, 브라우저가 PDF 뷰어로 열도록 inline 으로 준다.
     * 화면은 이걸 blob 으로 받아 iframe 에 띄운다(관리자 토큰이 필요해 S3 주소를 바로 쓰지 않는다)
     */
    @GetMapping("/files/{fileId}/view")
    public ResponseEntity<byte[]> view(@PathVariable Long fileId) {
        ChamMonimapCollectService.DownloadFile file = collectService.download(fileId);
        boolean pdf = file.fileName() != null && file.fileName().toLowerCase().endsWith(".pdf");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentDisposition(ContentDisposition.inline()
                .filename(file.fileName(), StandardCharsets.UTF_8)
                .build());
        headers.setContentType(pdf ? MediaType.APPLICATION_PDF : MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentLength(file.body().length);
        return new ResponseEntity<>(file.body(), headers, HttpStatus.OK);
    }

    /** 원본을 수동 업로드 양식(14열)으로 바꿔 내려준다 */
    @GetMapping("/files/{fileId}/upload-form")
    public ResponseEntity<byte[]> uploadForm(@PathVariable Long fileId,
                                             @RequestParam(required = false) String defaultName) {
        return attachment(collectService.uploadForm(fileId, defaultName));
    }

    // 한글 파일명이라 UTF-8 로 인코딩해 준다
    private ResponseEntity<byte[]> attachment(ChamMonimapCollectService.DownloadFile file) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentDisposition(ContentDisposition.attachment()
                .filename(file.fileName(), StandardCharsets.UTF_8)
                .build());
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentLength(file.body().length);
        return new ResponseEntity<>(file.body(), headers, HttpStatus.OK);
    }

    @GetMapping("/jobs")
    public PageResponse<CollectJobResponse> getJobs(@PageableDefault(size = 10) Pageable pageable) {
        return collectService.selectJobs(pageable);
    }

    @PostMapping("/jobs")
    public ApiResponse requestJob(@RequestBody(required = false) CollectJobRequest request,
                                  @AuthenticationPrincipal Jwt jwt) {
        return collectService.requestJob(request, memberId(jwt));
    }

    private Long memberId(Jwt jwt) {
        return Optional.ofNullable(jwt)
                .map(token -> token.getClaim("id"))
                .map(Object::toString)
                .map(Long::valueOf)
                .orElse(null);
    }
}
