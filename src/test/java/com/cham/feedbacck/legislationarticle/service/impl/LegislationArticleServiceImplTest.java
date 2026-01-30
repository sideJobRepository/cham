package com.cham.feedbacck.legislationarticle.service.impl;

import com.cham.RepositoryAndServiceTestSupport;
import com.cham.feedbacck.legislationarticle.service.LegislationArticleService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;

import java.io.FileInputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class LegislationArticleServiceImplTest extends RepositoryAndServiceTestSupport {
    
    @Autowired
    private LegislationArticleService legislationArticleService;
    
    @DisplayName("")
    @Test
    void test1() {
        try (FileInputStream file = new FileInputStream("src/test/java/com/cham/대전.xlsx")) {
            MockMultipartFile mockMultipartFile = new MockMultipartFile(
                    "file",
                    "sample.xlsx",
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", // 엑셀파일인지 검증
                    file
            );
            
            legislationArticleService.insertExcel(mockMultipartFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}