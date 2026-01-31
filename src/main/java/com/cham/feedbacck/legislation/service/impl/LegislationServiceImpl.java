package com.cham.feedbacck.legislation.service.impl;

import com.cham.feedbacck.legislation.dto.response.LegislationFullResponse;
import com.cham.feedbacck.legislation.entity.Legislation;
import com.cham.feedbacck.legislation.repository.LegislationRepository;
import com.cham.feedbacck.legislation.service.LegislationService;
import com.cham.feedbacck.legislationarticle.entity.LegislationArticle;
import com.cham.feedbacck.legislationarticle.repository.LegislationArticleRepository;
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
    
    private static final String NO_SECTION = "NO_SECTION";
    private static final String NO_CHAPTER = "NO_CHAPTER";
    
   
    @Override
    public LegislationFullResponse getAllFullLegislations() {

        List<Legislation> legislations = legislationRepository.findAll();

        List<LegislationFullResponse.Legislation> result =
                legislations.stream()
                        .map(this::buildLegislation)
                        .toList();

        return new LegislationFullResponse(result);
    }
    
    private LegislationFullResponse.Legislation buildLegislation(Legislation legislation) {
        
        // 1. 법안마다 조문 조회 (정렬 보장)
        List<LegislationArticle> articles =
                legislationArticleRepository.findByLegislationOrderByOrdersNo(legislation);
        
        // 2. part → chapter → section → articles
        Map<String, Map<String, Map<String, List<LegislationArticle>>>> grouped =
                articles.stream()
                        .collect(Collectors.groupingBy(
                                LegislationArticle::getPart,
                                LinkedHashMap::new,
                                Collectors.groupingBy(
                                        a -> a.getChapter() == null ? NO_CHAPTER : a.getChapter(),
                                        LinkedHashMap::new,
                                        Collectors.groupingBy(
                                                a -> a.getSection() == null ? NO_SECTION : a.getSection(),
                                                LinkedHashMap::new,
                                                Collectors.toList()
                                        )
                                )
                        ));
        
        // 3. Map → DTO 트리 변환
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
                                                                                a.getCategorySub()
                                                                        ))
                                                                        .toList()
                                                        ))
                                                        .toList()
                                        ))
                                        .toList()
                        ))
                        .toList();
        
        // 4. 최종 응답
        return new LegislationFullResponse.Legislation(
                legislation.getId(),
                legislation.getTitle(),
                legislation.getBillVersion(),
                parts
        );
    }
}
