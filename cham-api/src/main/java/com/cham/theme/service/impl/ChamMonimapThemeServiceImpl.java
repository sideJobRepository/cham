package com.cham.theme.service.impl;

import com.cham.config.S3FileUtils;
import com.cham.dto.response.ApiResponse;
import com.cham.file.UploadResult;
import com.cham.file.entity.ChamMonimapCommonFile;
import com.cham.file.enumeration.ChamMonimapFileType;
import com.cham.file.repository.ChamMonimapCommonFileRepository;
import com.cham.theme.dto.request.ThemePostRequest;
import com.cham.theme.dto.request.ThemePutRequest;
import com.cham.theme.dto.response.ThemeGetResponse;
import com.cham.theme.entity.ChamMonimapTheme;
import com.cham.theme.enumeration.ChamMonimapFlag;
import com.cham.theme.respotiroy.ChamMonimapThemeRepository;
import com.cham.theme.service.ChamMonimapThemeService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;

@Service
@Transactional
@RequiredArgsConstructor
public class ChamMonimapThemeServiceImpl implements ChamMonimapThemeService {

    private final ChamMonimapThemeRepository chamMonimapThemeRepository;
    
    private final ChamMonimapCommonFileRepository chamMonimapCommonFileRepository;
    
    private final S3FileUtils s3FileUtils;
    

    
    @Override
    public List<ThemeGetResponse> getThemes() {
         return chamMonimapThemeRepository.findByThemes();
    }
    
    @Override
    public ApiResponse saveTheme(List<ThemePostRequest> request) {
        
        List<Long> targetIds = request.stream()
                .map(ThemePostRequest::getTargetId)
                .filter(Objects::nonNull)
                .toList();
        
        HashSet<Long> uniqueIds  = new HashSet<>(targetIds);
        
        if(targetIds.size() != uniqueIds.size()) {
            throw new IllegalArgumentException("요청 내에 중복된 직위가 있습니다.");
        }
        
        
        for (ThemePostRequest themePostRequest : request) {
            if (themePostRequest.getFlag() == ChamMonimapFlag.C) {
                ChamMonimapTheme theme = ChamMonimapTheme
                        .builder()
                        .targetId(themePostRequest.getTargetId())
                        .color(themePostRequest.getColor())
                        .type(themePostRequest.getType())
                        .inputValue(themePostRequest.getInputValue())
                        .build();
                ChamMonimapTheme saveTheme = chamMonimapThemeRepository.save(theme);
                
                MultipartFile file = themePostRequest.getFile();
                if(file != null) {
                    UploadResult result = s3FileUtils.storeFile(file);
                    
                    ChamMonimapCommonFile commonFile = ChamMonimapCommonFile
                            .builder()
                            .fileName(result.getOriginalFilename())
                            .targetId(saveTheme.getId())
                            .fileUrl(result.getUrl())
                            .fileType(ChamMonimapFileType.THEME)
                            .uuidName(result.getUuid())
                            .build();
                    chamMonimapCommonFileRepository.save(commonFile);
                }
            } else if (themePostRequest.getFlag() == ChamMonimapFlag.P) {
                Long themeId = themePostRequest.getThemeId();
                ChamMonimapTheme theme = chamMonimapThemeRepository.findById(themeId).orElseThrow(() -> new IllegalArgumentException("존재 하지 않는 테마 ID 입니다."));
                theme.modify(themePostRequest);
                List<ChamMonimapCommonFile> commonFiles = chamMonimapCommonFileRepository.findTargetIds(themeId);
                if (!commonFiles.isEmpty()) {
                    for (ChamMonimapCommonFile commonFile : commonFiles) {
                        s3FileUtils.deleteFile(commonFile.getFileUrl());
                        chamMonimapCommonFileRepository.delete(commonFile);
                    }
                }
                if (themePostRequest.getFile() != null) {
                    UploadResult uploadResult = s3FileUtils.storeFile(themePostRequest.getFile());
                    ChamMonimapCommonFile modifyCommonFile = ChamMonimapCommonFile
                            .builder()
                            .fileName(uploadResult.getOriginalFilename())
                            .targetId(theme.getId())
                            .fileUrl(uploadResult.getUrl())
                            .fileType(ChamMonimapFileType.THEME)
                            .uuidName(uploadResult.getUuid())
                            .build();
                    chamMonimapCommonFileRepository.save(modifyCommonFile);
                }
            }
        }
        return new ApiResponse(200,true,"테마가 저장되었습니다.");
    }
    
    @Override
    public ApiResponse modifyTheme(List<ThemePutRequest> request) {
        
        List<Long> targetIds = request.stream()
                .map(ThemePutRequest::getTargetId)
                .filter(Objects::nonNull)
                .toList();
        
        HashSet<Long> uniqueIds  = new HashSet<>(targetIds);
        
        if(targetIds.size() != uniqueIds.size()) {
            throw new IllegalArgumentException("요청 내에 중복된 직위가 있습니다.");
        }
        for (ThemePutRequest themePutRequest : request) {
            Long themeId = themePutRequest.getThemeId();
            ChamMonimapTheme theme = chamMonimapThemeRepository.findById(themeId).orElseThrow(() -> new IllegalArgumentException("존재 하지 않는 테마 ID 입니다."));
         //   theme.modify(themePutRequest);
        }
        return new ApiResponse(200,true,"테마가 수정되었습니다.");
    }
    
    @Override
    public ApiResponse removeTheme(Long themeId) {
        ChamMonimapTheme theme = chamMonimapThemeRepository.findById(themeId).orElseThrow(() -> new IllegalArgumentException("존재 하지 않는 테마 ID 입니다."));
        List<ChamMonimapCommonFile> commonFiles = chamMonimapCommonFileRepository.findTargetIds(themeId);
        if (!commonFiles.isEmpty()) {
            for (ChamMonimapCommonFile commonFile : commonFiles) {
                s3FileUtils.deleteFile(commonFile.getFileUrl());
                chamMonimapCommonFileRepository.delete(commonFile);
            }
        }
        chamMonimapThemeRepository.delete(theme);
        return new ApiResponse(200,true,"테마가 삭제 되었습니다.");
    }
}
