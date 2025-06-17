package com.cham.service.impl;

import com.cham.RepositoryAndServiceTestSupport;
import com.cham.service.CardUseService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;

import java.io.FileInputStream;
import java.io.IOException;

class CardOwnerServiceImplTest extends RepositoryAndServiceTestSupport {


    
    @Autowired
    private CardUseService cardUseService;
    
    
    @DisplayName("엑셀파일 업로드")
    @Test
    void test() throws IOException {
        
        
        try(FileInputStream file = new FileInputStream("src/test/java/com/cham/더미데이터.xlsx")) {
            MockMultipartFile mockMultipartFile = new MockMultipartFile(
                    "file",
                    "sample.xlsx",
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", // 엑셀파일인지 검증
                    file
            );
            
            cardUseService.insertCardUse(mockMultipartFile);
        }
    }
}