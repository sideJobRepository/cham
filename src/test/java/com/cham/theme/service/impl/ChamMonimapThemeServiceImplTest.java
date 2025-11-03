package com.cham.theme.service.impl;

import com.cham.RepositoryAndServiceTestSupport;
import com.cham.dto.response.ApiResponse;
import com.cham.theme.dto.request.ThemePostRequest;
import com.cham.theme.dto.request.ThemePutRequest;
import com.cham.theme.dto.response.ThemeGetResponse;
import com.cham.theme.enumeration.ChamMonimapFlag;
import com.cham.theme.enumeration.ChamMonimapThemeType;
import com.cham.theme.service.ChamMonimapThemeService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ChamMonimapThemeServiceImplTest extends RepositoryAndServiceTestSupport {
    
    @Autowired
    private ChamMonimapThemeService service;
    
    @DisplayName("테마 저장")
    @Test
    void test1() throws IOException {
        
        File file1 = new File("src/test/java/com/cham/이미지1.png");
        File file2 = new File("src/test/java/com/cham/이미지2.png");

        MockMultipartFile mockFile1 = new MockMultipartFile(
                "file", file1.getName(), "image/png", new FileInputStream(file1)
        );

        MockMultipartFile mockFile2 = new MockMultipartFile(
                "file", file2.getName(), "image/png", new FileInputStream(file2)
        );
        Long targetId = 7L;
        String color = "#FF0000"; //빨강색
        ChamMonimapThemeType owner = ChamMonimapThemeType.OWNER;
        String inputValue1 = "ㅁㄴㅇㅁaaa";
        ChamMonimapFlag flag1 = ChamMonimapFlag.C;
        ThemePostRequest themePostRequest = new ThemePostRequest(3L,targetId, color, inputValue1,owner,flag1,mockFile1);
        
        Long targetId2 = null;
        String color2 = "#FFD700"; // 골드
        ChamMonimapThemeType input = ChamMonimapThemeType.INPUT;
        String inputValue2 = "ddd";
        ChamMonimapFlag flag2 = ChamMonimapFlag.C;
        ThemePostRequest themePostRequest2 = new ThemePostRequest(4L,targetId2, color2, inputValue2,input,flag2,mockFile2);
        List<ThemePostRequest> themePostRequest1 = List.of(themePostRequest, themePostRequest2);
        ApiResponse apiResponse = service.saveTheme(themePostRequest1);
        assertThat(apiResponse).isNotNull();
    }
    
    @DisplayName("테마 수정")
    @Test
    void test2(){
        Long themeId = 1L;
        Long targetId = 6L;
        String color = "#FFD700";
        ChamMonimapThemeType owner = ChamMonimapThemeType.OWNER;
        String inputValue1 = null;
        ThemePutRequest themePutRequest = new ThemePutRequest(themeId, targetId, color, inputValue1,owner);
        
        Long themeId2 = 2L;
        Long targetId2 = null;
        String color2 = "#FF0000";
        ChamMonimapThemeType input = ChamMonimapThemeType.INPUT;
        String inputValue2 = "음료";
        ThemePutRequest themePutRequest2 = new ThemePutRequest(themeId2, targetId2, color2, inputValue2,input);
        List<ThemePutRequest> themePutRequest1 = List.of(themePutRequest, themePutRequest2);
        ApiResponse apiResponse = service.modifyTheme(themePutRequest1);
        assertThat(apiResponse).isNotNull();
    }
    
    @DisplayName("테마 삭제")
    @Test
    void test3(){
        ApiResponse apiResponse = service.removeTheme(11L);
        assertThat(apiResponse).isNotNull();
    }
    
    @DisplayName("테마 조회")
    @Test
    void test4(){
        List<ThemeGetResponse> themes = service.getThemes();
        System.out.println("themes = " + themes);
    }
}