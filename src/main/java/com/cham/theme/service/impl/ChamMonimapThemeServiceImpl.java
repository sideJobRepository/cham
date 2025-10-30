package com.cham.theme.service.impl;

import com.cham.dto.response.ApiResponse;
import com.cham.theme.dto.request.ThemePostRequest;
import com.cham.theme.dto.request.ThemePutRequest;
import com.cham.theme.dto.response.ThemeGetResponse;
import com.cham.theme.entity.ChamMonimapTheme;
import com.cham.theme.respotiroy.ChamMonimapThemeRepository;
import com.cham.theme.service.ChamMonimapThemeService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;

@Service
@Transactional
@RequiredArgsConstructor
public class ChamMonimapThemeServiceImpl implements ChamMonimapThemeService {

    private final ChamMonimapThemeRepository chamMonimapThemeRepository;
    
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
        
        if (!targetIds.isEmpty()) {
            List<Long> duplicatedIds = chamMonimapThemeRepository.findByDuplicationCheckTargetId(targetIds);
            if (!duplicatedIds.isEmpty()) {
                throw new IllegalArgumentException(
                        "이미 테마가 지정된 직위가 있습니다: " + duplicatedIds
                );
            }
        }
        List<ChamMonimapTheme> themes = request.stream()
                .map(item -> ChamMonimapTheme
                        .builder()
                        .targetId(item.getTargetId())
                        .color(item.getColor())
                        .type(item.getType())
                        .inputValue(item.getInputValue())
                        .build()
                ).toList();
        chamMonimapThemeRepository.saveAll(themes);
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
            theme.modify(themePutRequest);
        }
        return new ApiResponse(200,true,"테마가 수정되었습니다.");
    }
    
    @Override
    public ApiResponse removeTheme(Long themeId) {
        ChamMonimapTheme theme = chamMonimapThemeRepository.findById(themeId).orElseThrow(() -> new IllegalArgumentException("존재 하지 않는 테마 ID 입니다."));
        chamMonimapThemeRepository.delete(theme);
        return new ApiResponse(200,true,"테마가 삭제 되었습니다.");
    }
}
