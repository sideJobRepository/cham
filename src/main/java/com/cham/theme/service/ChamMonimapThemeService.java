package com.cham.theme.service;

import com.cham.dto.response.ApiResponse;
import com.cham.theme.dto.request.ThemePostRequest;
import com.cham.theme.dto.request.ThemePutRequest;
import com.cham.theme.dto.response.ThemeGetResponse;

import java.util.List;

public interface ChamMonimapThemeService {
    
    
    List<ThemeGetResponse> getThemes();
    
    ApiResponse saveTheme(List<ThemePostRequest> request);
    
    ApiResponse modifyTheme(List<ThemePutRequest> request);
    
    ApiResponse removeTheme(Long themeId);
}
