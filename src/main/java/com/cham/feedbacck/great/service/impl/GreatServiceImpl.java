package com.cham.feedbacck.great.service.impl;

import com.cham.dto.response.ApiResponse;
import com.cham.feedbacck.great.dto.request.GreatPostRequest;
import com.cham.feedbacck.great.dto.request.GreatPutRequest;
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

@Service
@Transactional
@RequiredArgsConstructor
public class GreatServiceImpl implements GreatService {
    
    private final GreatRepository greatRepository;
    private final ChamMonimapMemberRepository monimapMemberRepository;
    private final LegislationArticleRepository legislationArticleRepository;
    
    
    @Override
    public GreatResponse getGreats(Long articleId, Long memberId) {
        
        
        // 기본값 0
        Map<GreatType, Long> countMap = Arrays.stream(GreatType.values())
                                        .collect(Collectors.toMap(t -> t, t -> 0L));
        
        //  집계 반영
        greatRepository.findGreatCounts(articleId)
                .forEach(c -> countMap.put(c.getGreatType(), c.getCount()));
        
        // 3) 내 선택 (로그인한 경우만)
        GreatType selectedType = null;
        if (memberId != null) {
            selectedType = greatRepository.findMyGreatType(articleId, memberId);
        }
        
        // 최종 응답
        return GreatResponse.builder()
                .articleId(articleId)
                .supportCount(countMap.get(GreatType.SUPPORT))
                .oppositionCount(countMap.get(GreatType.OPPOSITION))
                .concernCount(countMap.get(GreatType.CONCERN))
                .selectedType(selectedType)
                .build();
        
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
        return null;
    }
}
