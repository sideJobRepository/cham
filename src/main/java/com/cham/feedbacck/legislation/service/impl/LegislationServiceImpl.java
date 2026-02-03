package com.cham.feedbacck.legislation.service.impl;

import com.cham.feedbacck.legislation.dto.response.LegislationFullResponse;
import com.cham.feedbacck.legislation.entity.Legislation;
import com.cham.feedbacck.legislation.repository.LegislationRepository;
import com.cham.feedbacck.legislation.service.LegislationService;
import com.cham.feedbacck.legislationarticle.entity.LegislationArticle;
import com.cham.feedbacck.legislationarticle.repository.LegislationArticleRepository;
import com.cham.feedbacck.reply.repository.LegislationArticleReplyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class LegislationServiceImpl implements LegislationService {

    
    private final LegislationRepository legislationRepository;
    private final LegislationArticleRepository legislationArticleRepository;
    private final LegislationArticleReplyRepository legislationArticleReplyRepository;
    
    private static final String NO_SECTION = "NO_SECTION";
    private static final String NO_CHAPTER = "NO_CHAPTER";
    private static final String NO_PART = "NO_PART";
    
    
    @Override
    public LegislationFullResponse getAllFullLegislations() {

        List<Legislation> legislations = legislationRepository.findAll();

        List<LegislationFullResponse.Legislation> result =
                legislations.stream()
                        .map(this::buildLegislation)
                        .toList();

        return new LegislationFullResponse(result);
    }
    
    @Override
    public LegislationFullResponse searchLegislations(String keyword) {
        // 1. 조문 기준 검색
     List<LegislationArticle> articles = legislationRepository.searchArticlesByKeyword(keyword);
 
     if (articles.isEmpty()) {
         return new LegislationFullResponse(List.of());
     }
 
     // 2. legislation 기준 그룹핑
     Map<Legislation, List<LegislationArticle>> grouped =
             articles.stream()
                     .collect(Collectors.groupingBy(
                             LegislationArticle::getLegislation,
                             LinkedHashMap::new,
                             Collectors.toList()
                     ));
 
     // 3. legislation 단위로 트리 조립
     List<LegislationFullResponse.Legislation> result =
             grouped.entrySet().stream()
                     .map(entry -> buildLegislationWithFilteredArticles(entry.getKey(), entry.getValue())
                     )
                     .toList();
 
     // 4. 최종 응답
     return new LegislationFullResponse(result);
    }
    
    private LegislationFullResponse.Legislation buildLegislation(
            Legislation legislation) {
    
        // 1. 법안마다 조문 조회 (정렬 보장)
        List<LegislationArticle> articles =
                legislationArticleRepository
                        .findByLegislationOrderByOrdersNo(legislation);
    
        // 2. 댓글 개수 Map
        Map<Long, Long> replyCountMap = getReplyCountMap(articles);
        
Map<String, Map<String, Map<String, List<LegislationArticle>>>> grouped =
           articles.stream()
                   .collect(Collectors.groupingBy(
                           a -> a.getPart() == null
                                   ? NO_PART
                                   : a.getPart(),
                           LinkedHashMap::new,
                           Collectors.groupingBy(
                                   a -> a.getChapter() == null
                                           ? NO_CHAPTER
                                           : a.getChapter(),
                                   LinkedHashMap::new,
                                   Collectors.groupingBy(
                                           a -> a.getSection() == null
                                                   ? NO_SECTION
                                                   : a.getSection(),
                                           LinkedHashMap::new,
                                           Collectors.toList()
                                   )
                           )
                   ));
    
        // 4. Map → DTO 트리 변환
        List<LegislationFullResponse.Part> parts =
                grouped.entrySet().stream()
                        .map(partEntry -> new LegislationFullResponse.Part(
                                partEntry.getKey(),
                                partEntry.getValue().entrySet().stream()
                                        .map(chapterEntry -> new LegislationFullResponse.Chapter(
                                                NO_CHAPTER.equals(chapterEntry.getKey())
                                                        ? null
                                                        : chapterEntry.getKey(),
                                                chapterEntry.getValue().entrySet().stream()
                                                        .map(sectionEntry -> new LegislationFullResponse.Section(
                                                                NO_SECTION.equals(sectionEntry.getKey())
                                                                        ? null
                                                                        : sectionEntry.getKey(),
                                                                sectionEntry.getValue().stream()
                                                                        .map(a -> new LegislationFullResponse.Article(
                                                                                a.getId(),
                                                                                a.getArticleNo(),
                                                                                a.getArticleTitle(),
                                                                                a.getCont(),
                                                                                a.getCategoryMain(),
                                                                                a.getCategorySub(),
                                                                                replyCountMap.getOrDefault(
                                                                                        a.getId(), 0L
                                                                                )
                                                                        ))
                                                                        .toList()
                                                        ))
                                                        .toList()
                                        ))
                                        .toList()
                        ))
                        .toList();
    
        return new LegislationFullResponse.Legislation(
                legislation.getId(),
                legislation.getTitle(),
                legislation.getBillVersion(),
                parts
        );
    }
  
    private LegislationFullResponse.Legislation buildLegislationWithFilteredArticles(
            Legislation legislation,
            List<LegislationArticle> articles
    ) {
    
        // 댓글 개수 Map
        Map<Long, Long> replyCountMap = getReplyCountMap(articles);
    
        Map<String, Map<String, Map<String, List<LegislationArticle>>>> grouped =
                articles.stream()
                        .collect(Collectors.groupingBy(
                                LegislationArticle::getPart,
                                LinkedHashMap::new,
                                Collectors.groupingBy(
                                        a -> a.getChapter() == null
                                                ? NO_CHAPTER
                                                : a.getChapter(),
                                        LinkedHashMap::new,
                                        Collectors.groupingBy(
                                                a -> a.getSection() == null
                                                        ? NO_SECTION
                                                        : a.getSection(),
                                                LinkedHashMap::new,
                                                Collectors.toList()
                                        )
                                )
                        ));
    
        List<LegislationFullResponse.Part> parts =
                grouped.entrySet().stream()
                        .map(partEntry -> new LegislationFullResponse.Part(
                                partEntry.getKey(),
                                partEntry.getValue().entrySet().stream()
                                        .map(chapterEntry -> new LegislationFullResponse.Chapter(
                                                NO_CHAPTER.equals(chapterEntry.getKey())
                                                        ? null
                                                        : chapterEntry.getKey(),
                                                chapterEntry.getValue().entrySet().stream()
                                                        .map(sectionEntry -> new LegislationFullResponse.Section(
                                                                NO_SECTION.equals(sectionEntry.getKey())
                                                                        ? null
                                                                        : sectionEntry.getKey(),
                                                                sectionEntry.getValue().stream()
                                                                        .map(a -> new LegislationFullResponse.Article(
                                                                                a.getId(),
                                                                                a.getArticleNo(),
                                                                                a.getArticleTitle(),
                                                                                a.getCont(),
                                                                                a.getCategoryMain(),
                                                                                a.getCategorySub(),
                                                                                replyCountMap.getOrDefault(
                                                                                        a.getId(), 0L
                                                                                )
                                                                        ))
                                                                        .toList()
                                                        ))
                                                        .toList()
                                        ))
                                        .toList()
                        ))
                        .toList();
    
        return new LegislationFullResponse.Legislation(
                legislation.getId(),
                legislation.getTitle(),
                legislation.getBillVersion(),
                parts
        );
    }
    
    private Map<Long, Long> getReplyCountMap(List<LegislationArticle> articles) {
    
        List<Object[]> rows = legislationArticleReplyRepository.countByArticles(articles);
    
        return rows.stream()
                .collect(Collectors.toMap(
                        r -> (Long) r[0],   // articleId
                        r -> (Long) r[1]    // count
                ));
    }
    
}
