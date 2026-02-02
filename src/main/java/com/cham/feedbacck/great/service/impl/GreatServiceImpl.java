package com.cham.feedbacck.great.service.impl;

import com.cham.dto.response.ApiResponse;
import com.cham.feedbacck.great.dto.request.GreatGetPostRequest;
import com.cham.feedbacck.great.dto.request.GreatPostRequest;
import com.cham.feedbacck.great.dto.request.GreatPutRequest;
import com.cham.feedbacck.great.dto.response.GreatMyTypeProjection;
import com.cham.feedbacck.great.dto.response.GreatResponse;
import com.cham.feedbacck.great.entity.Great;
import com.cham.feedbacck.great.enums.GreatType;
import com.cham.feedbacck.great.repository.GreatRepository;
import com.cham.feedbacck.great.service.GreatService;
import com.cham.feedbacck.legislationarticle.entity.LegislationArticle;
import com.cham.feedbacck.legislationarticle.repository.LegislationArticleRepository;
import com.cham.member.entity.ChamMonimapMember;
import com.cham.member.repository.ChamMonimapMemberRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Transactional
@RequiredArgsConstructor
@Service
public class GreatServiceImpl implements GreatService {
    
    private final GreatRepository greatRepository;
    private final ChamMonimapMemberRepository monimapMemberRepository;
    private final LegislationArticleRepository legislationArticleRepository;
    
    
    @Override
    public List<GreatResponse> getGreats(GreatGetPostRequest request, Long memberId) {
        
        List<Long> articleIds = request.getArticleIds();
    
        // 1. articleId별 기본 countMap 초기화
        Map<Long, Map<GreatType, Long>> countMapByArticle =
                articleIds.stream()
                        .collect(Collectors.toMap(
                                id -> id,
                                id -> Arrays.stream(GreatType.values())
                                        .collect(Collectors.toMap(t -> t, t -> 0L))
                        ));
    
        // 2. 집계 반영
        greatRepository.findGreatCounts(articleIds)
                .forEach(c -> {
                    Map<GreatType, Long> map = countMapByArticle.get(c.getArticleId());
                    if (map != null) {
                        map.put(c.getGreatType(), c.getCount());
                    }
                });
    
        // 3. 내가 선택한 타입 (로그인한 경우)
        Map<Long, GreatType> mySelectedMap =
                (memberId == null)
                        ? Map.of()
                        : greatRepository.findMyGreatType(articleIds, memberId)
                                .stream()
                                .collect(Collectors.toMap(
                                        GreatMyTypeProjection::getArticleId,
                                        GreatMyTypeProjection::getGreatType
                                ));
    
        // 4. articleId별 응답 생성
        return articleIds.stream()
                .map(articleId -> {
                    Map<GreatType, Long> countMap = countMapByArticle.get(articleId);
    
                    return GreatResponse.builder()
                            .articleId(articleId)
                            .supportCount(countMap.get(GreatType.SUPPORT))
                            .oppositionCount(countMap.get(GreatType.OPPOSITION))
                            .concernCount(countMap.get(GreatType.CONCERN))
                            .selectedType(mySelectedMap.get(articleId))
                            .build();
                })
                .toList();
        
    }
    
    @Override
    public ApiResponse createGreat(GreatPostRequest request) {
        Long memberId = request.getMemberId();
        Long articleId = request.getArticleId();
        ChamMonimapMember member = monimapMemberRepository.findById(memberId).orElseThrow(() -> new RuntimeException("존재하지않는 회원입니다."));
        LegislationArticle article = legislationArticleRepository.findById(articleId).orElseThrow(() -> new RuntimeException("존재하지않는 조문입니다."));
        
        Great great = Great.builder()
                .member(member)
                .legislationArticle(article)
                .greatType(request.getGreatType())
                .build();
        greatRepository.save(great);
        return new ApiResponse(200, true, "저장 되었습니다.");
    }
    
    @Override
    public ApiResponse updateGreat(GreatPutRequest request) {
        Long id = request.getId();
        Great great = greatRepository.findById(id).orElseThrow(() -> new RuntimeException("존재하지 않은 좋아요 입니다."));
        great.modify(request.getGreatType());
        return new ApiResponse(200,true,"수정되었습니다.");
    }
}
