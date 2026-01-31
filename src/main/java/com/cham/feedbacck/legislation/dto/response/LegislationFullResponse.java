package com.cham.feedbacck.legislation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

//제1편 총칙        ← part
// └─ 제1조 목적    ← article_no
//
//제2편 충남대전통합특별시의 설치   ← part
// └─ 제1절 충남대전통합특별시의 설치  ← section
//     ├─ 제3조 ○○
//     ├─ 제4조 ○○
// └─ 제2절 관할구역의 지정          ← section
//     ├─ 제5조 ○○


@Getter
@AllArgsConstructor
public class LegislationFullResponse {
    
    private List<Legislation> legislations;
 
     /* ===== NESTED ===== */
 
     @Getter
     @AllArgsConstructor
     public static class Legislation {
         private Long id;
         private String title;
         private String billVersion;
         private List<Part> parts;
     }
 
     @Getter
     @AllArgsConstructor
     public static class Part {
         private String part;
         private List<Chapter> chapters;
     }
    @Getter
    @AllArgsConstructor
    public static class Chapter {
        private String chapter;
        private List<Section> sections;
    }
    
    
    @Getter
     @AllArgsConstructor
     public static class Section {
         private String section; // null 가능
         private List<Article> articles;
     }
 
     @Getter
     @AllArgsConstructor
     public static class Article {
         private Long articleId;
         private String articleNo;
         private String articleTitle;
         private String content;
         private String categoryMain;
         private String categorySub;
     }
}
