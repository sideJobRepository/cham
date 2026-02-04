package com.cham.feedbacck.legislationarticle.service.impl;

import com.cham.RepositoryAndServiceTestSupport;
import com.cham.feedbacck.legislation.dto.response.LegislationFullResponse;
import com.cham.feedbacck.legislation.dto.response.LegislationReplyOnlyResponse;
import com.cham.feedbacck.legislation.service.LegislationService;
import com.cham.feedbacck.legislationarticle.service.LegislationArticleService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;

import java.io.FileInputStream;
import java.io.IOException;

class LegislationArticleServiceImplTest extends RepositoryAndServiceTestSupport {
    
    @Autowired
    private LegislationArticleService legislationArticleService;
    
    
    @Autowired
    private LegislationService legislationService;
    
    
    @DisplayName("")
    @Test
    void test1(){
        LegislationReplyOnlyResponse legislationRepliesOnly = legislationService.getLegislationRepliesOnly(2L, null);
        System.out.println("legislationRepliesOnly = " + legislationRepliesOnly);
    }
    
    @DisplayName("")
    @Test
    void test2() {
        try (FileInputStream file = new FileInputStream("src/test/java/com/cham/광주전남.xlsx")) {
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
   
    @DisplayName("")
    @Test
    void test3(){
        LegislationFullResponse test = legislationService.searchLegislations("이 법은 제8조에 따른 통합특별시의 관할구역에만 적용한다.");
        System.out.println("test = " + test);
        
    }
}