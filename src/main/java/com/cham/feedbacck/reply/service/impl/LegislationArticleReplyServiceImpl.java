package com.cham.feedbacck.reply.service.impl;


import com.cham.dto.response.ApiResponse;
import com.cham.feedbacck.legislationarticle.entity.LegislationArticle;
import com.cham.feedbacck.legislationarticle.repository.LegislationArticleRepository;
import com.cham.feedbacck.reply.dto.request.LegislationArticleReplyPostRequest;
import com.cham.feedbacck.reply.dto.request.LegislationArticleReplyPutRequest;
import com.cham.feedbacck.reply.dto.response.LegislationArticleReplyGetRequest;
import com.cham.feedbacck.reply.entity.LegislationArticleReply;
import com.cham.feedbacck.reply.repository.LegislationArticleReplyRepository;
import com.cham.feedbacck.reply.service.LegislationArticleReplyService;
import com.cham.member.entity.ChamMonimapMember;
import com.cham.member.repository.ChamMonimapMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
@RequiredArgsConstructor
public class LegislationArticleReplyServiceImpl implements LegislationArticleReplyService {
    
    private final LegislationArticleRepository articleRepository;
    private final LegislationArticleReplyRepository replyRepository;
    private final ChamMonimapMemberRepository memberRepository;
    
    @Override
    @Transactional(readOnly = true)
    public LegislationArticleReplyGetRequest getReplies(Long articleId, Long loginMemberId) {
        List<LegislationArticleReply> replies = replyRepository.findRepliesByArticleId(articleId);
        
        // 1. 부모 댓글 Map
        Map<Long, LegislationArticleReplyGetRequest.Reply> parentMap = new LinkedHashMap<>();
        
        // 2. 결과 List
        List<LegislationArticleReplyGetRequest.Reply> result = new ArrayList<>();
        
        for (LegislationArticleReply reply : replies) {
            
            boolean isOwner = reply.getMember().getChamMonimapMemberId().equals(loginMemberId);
            
            // 부모 댓글
            if (reply.getParent() == null) {
                LegislationArticleReplyGetRequest.Reply parent =
                        LegislationArticleReplyGetRequest.Reply.builder()
                                .replyId(reply.getId())
                                .memberId(reply.getMember().getChamMonimapMemberId())
                                .memberName(reply.getMember().getChamMonimapMemberName())
                                .content(reply.getDelStatus() ? "삭제된 댓글입니다." : reply.getContent())
                                .title(reply.getArticle().getArticleNo() + " " + reply.getArticle().getArticleTitle())
                                .registDate(reply.getRegistDate())
                                .isOwner(isOwner)
                                .children(new ArrayList<>())
                                .build();
                
                parentMap.put(reply.getId(), parent);
                result.add(parent);
            }
            // 대댓글
            else {
                LegislationArticleReplyGetRequest.Reply parent =
                        parentMap.get(reply.getParent().getId());
                
                if (parent == null) continue; // 방어
                
                parent.getChildren().add(
                        LegislationArticleReplyGetRequest.Reply.builder()
                                .replyId(reply.getId())
                                .memberId(reply.getMember().getChamMonimapMemberId())
                                .memberName(reply.getMember().getChamMonimapMemberName())
                                .content(reply.getDelStatus() ? "삭제된 댓글입니다." : reply.getContent())
                                .registDate(reply.getRegistDate())
                                .isOwner(isOwner)
                                .children(List.of()) // 대댓글의 대댓글은 없음
                                .build()
                );
            }
        }
        
        return LegislationArticleReplyGetRequest.builder()
                .articleId(articleId)
                .replies(result)
                .build();
    }
    
    @Override
    public ApiResponse createReply(LegislationArticleReplyPostRequest request, Long memberId) {
        // 1. 작성자
        ChamMonimapMember member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원 없음"));
        
        // 2. 조문
        LegislationArticle article = articleRepository.findById(
                        request.getArticleId())
                .orElseThrow(() -> new IllegalArgumentException("조문 없음"));
        
        // 3. 부모 댓글 (대댓글일 경우)
        LegislationArticleReply parent = null;
        if (request.getParentReplyId() != null) {
            parent = replyRepository.findById(request.getParentReplyId())
                    .orElseThrow(() -> new IllegalArgumentException("부모 댓글 없음"));
            
            //안전장치: 같은 조문인지 검증
            if (!parent.getArticle().getId().equals(article.getId())) {
                throw new IllegalArgumentException("조문이 다른 댓글에는 답글을 달 수 없습니다.");
            }
        }
        
        LegislationArticleReply reply = LegislationArticleReply.builder()
                .member(member)
                .article(article)
                .parent(parent)
                .content(request.getContent())
                .delStatus(false)
                .build();
        
        replyRepository.save(reply);
        return new ApiResponse(200, true, "댓글이 작성되었습니다.");
    }
    
    @Override
    public ApiResponse modifyReply(LegislationArticleReplyPutRequest request) {
        Long replyId = request.getReplyId();
        LegislationArticleReply reply = replyRepository.findById(replyId).orElseThrow(() -> new RuntimeException("존재 하지 않는 댓글입니다."));
        reply.modify(request);
        return new ApiResponse(200, true, "댓글이 수정되었습니다.");
    }
    
    
    @Override
    public ApiResponse deleteReply(Long replyId) {
        LegislationArticleReply reply = replyRepository.findById(replyId).orElseThrow(() -> new RuntimeException("존재 하지 않는 댓글입니다."));
        reply.modifyStatus();
        return new ApiResponse(200, true, "댓글이 삭제되었습니다");
    }
}
