package com.cham.theme.controller;


import com.cham.config.S3FileUtils;
import com.cham.dto.response.ApiResponse;
import com.cham.theme.dto.request.ThemePostRequestWrapper;
import com.cham.theme.dto.request.ThemePutRequest;
import com.cham.theme.dto.response.ThemeGetResponse;
import com.cham.theme.service.ChamMonimapThemeService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/cham")
@RequiredArgsConstructor
public class ChamMonimapThemeController {

    private final ChamMonimapThemeService chamMonimapThemeService;
    
    private final S3Client s3Client;
    
    @Value("${spring.cloud.aws.s3.bucket}")
     private String bucketName;

    @GetMapping("/theme")
    public List<ThemeGetResponse> getThemeList() {
        return chamMonimapThemeService.getThemes();
    }

    @PostMapping("/theme")
    public ApiResponse saveTheme(@ModelAttribute ThemePostRequestWrapper wrapper) {
        return chamMonimapThemeService.saveTheme(wrapper.getThemes());
    }

    @PutMapping("/theme")
    public ApiResponse updateTheme(@ModelAttribute List<ThemePutRequest> requests) {
        return chamMonimapThemeService.modifyTheme(requests);
    }

    @DeleteMapping("/theme/{id}")
    public ApiResponse deleteTheme(@PathVariable Long id) {
        return chamMonimapThemeService.removeTheme(id);
    }
    
    @GetMapping("/theme/file")
    public ResponseEntity<byte[]> getThemeFile(@RequestParam String url) throws IOException {
        String key = url.substring(url.lastIndexOf("/") + 1);
        ResponseInputStream<GetObjectResponse> object = s3Client.getObject(
                GetObjectRequest.builder()
                        .bucket(bucketName)
                        .key(key)
                        .build()
        );
    
        byte[] fileBytes = object.readAllBytes();
    
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(object.response().contentType())); // 예: image/png
        headers.setContentLength(fileBytes.length);
        headers.setContentDispositionFormData("inline", url); // attachment -> 다운로드, inline -> 미리보기
    
        return new ResponseEntity<>(fileBytes, headers, HttpStatus.OK);

    }
}
