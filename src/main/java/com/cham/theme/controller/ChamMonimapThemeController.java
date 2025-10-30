package com.cham.theme.controller;


import com.cham.dto.response.ApiResponse;
import com.cham.theme.dto.request.ThemePostRequest;
import com.cham.theme.dto.request.ThemePutRequest;
import com.cham.theme.dto.response.ThemeGetResponse;
import com.cham.theme.service.ChamMonimapThemeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/cham")
@RequiredArgsConstructor
public class ChamMonimapThemeController {

    private final ChamMonimapThemeService chamMonimapThemeService;
    
    @GetMapping("/theme")
    public List<ThemeGetResponse> getThemeList() {
        return chamMonimapThemeService.getThemes();
    }
    
    @PostMapping("/theme")
    public ApiResponse saveTheme(@RequestBody List<ThemePostRequest> requests) {
        return chamMonimapThemeService.saveTheme(requests);
    }
    
    @PutMapping("/theme")
    public ApiResponse updateTheme(@RequestBody List<ThemePutRequest> requests) {
        return chamMonimapThemeService.modifyTheme(requests);
    }
    
    @DeleteMapping("/theme/{id}")
    public ApiResponse deleteTheme(@PathVariable Long id) {
        return chamMonimapThemeService.removeTheme(id);
    }
}
