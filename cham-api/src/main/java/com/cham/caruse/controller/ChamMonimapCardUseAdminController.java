package com.cham.caruse.controller;

import com.cham.caruse.dto.CardUseUploadResponse;
import com.cham.caruse.service.ChamMonimapCardUseService;
import com.cham.dto.request.CardUseUploadDeleteKeyRequest;
import com.cham.dto.request.CardUseUploadPublicRequest;
import com.cham.dto.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 맛집지도 공개관리(관리자 전용).
 *
 * 접근 권한은 코드가 아니라 DB(CHAM_MONIMAP_URL_RESOURCES)에서 읽는다.
 * '공개여부 alter.sql' 의 INSERT 를 돌리고 서버를 재시작해야 ADMIN 전용이 된다.
 * 등록하지 않으면 ChamMonimapAuthorizationManager 가 허용으로 떨어져 누구나 호출할 수 있다.
 */
@RestController
@RequestMapping("/cham/admin")
@RequiredArgsConstructor
public class ChamMonimapCardUseAdminController {

    private final ChamMonimapCardUseService cardUseService;

    @GetMapping("/card-use-uploads")
    public List<CardUseUploadResponse> getCardUseUploads() {
        return cardUseService.selectCardUseUploads();
    }

    // 삭제키를 경로가 아닌 본문으로 받는다. 공백과 한글이 섞인 값이라 URL 인코딩이 번거롭다.
    @PutMapping("/card-use-uploads/public")
    public ApiResponse modifyCardUseUploadPublic(@Validated @RequestBody CardUseUploadPublicRequest request) {
        return cardUseService.modifyCardUseUploadPublic(request);
    }

    @PutMapping("/card-use-uploads/delete-key")
    public ApiResponse modifyCardUseUploadDeleteKey(@Validated @RequestBody CardUseUploadDeleteKeyRequest request) {
        return cardUseService.modifyCardUseUploadDeleteKey(request);
    }

    /**
     * 업로드 한 건을 엑셀로 내려준다. 열 구성은 업로드용과 같아서 고쳐서 다시 올릴 수 있다.
     * 다만 삭제키가 그대로 들어 있어 그 상태로는 '이미 존재하는 삭제키' 로 막힌다.
     * 원본을 갈아끼우려면 먼저 지우거나, 엑셀의 삭제키를 새 값으로 바꿔서 올린다.
     */
    @GetMapping("/card-use-uploads/excel")
    public ResponseEntity<byte[]> getCardUseUploadExcel(@RequestParam String deleteKey) {
        byte[] body = cardUseService.exportCardUseUpload(deleteKey);

        // 한글 삭제키가 그대로 파일명이 되므로 UTF-8 로 인코딩해 준다.
        ContentDisposition disposition = ContentDisposition.attachment()
                .filename(deleteKey + ".xlsx", StandardCharsets.UTF_8)
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentDisposition(disposition);
        headers.setContentType(MediaType.parseMediaType(
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.setContentLength(body.length);

        return new ResponseEntity<>(body, headers, HttpStatus.OK);
    }
}
