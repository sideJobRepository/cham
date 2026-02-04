package com.cham.feedbacck.reply.entity;

import com.cham.base.BaseData;
import com.cham.feedbacck.legislation.entity.Legislation;
import com.cham.feedbacck.legislationarticle.entity.LegislationArticle;
import com.cham.feedbacck.reply.dto.request.LegislationArticleReplyPutRequest;
import com.cham.member.entity.ChamMonimapMember;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "LEGISLATION_ARTICLE_REPLY")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class LegislationArticleReply extends BaseData {
    
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "LEGISLATION_ARTICLE_REPLY_ID")
    private Long id;
    
    // 작성자 (회원)
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CHAM_MONIMAP_MEMBER_ID")
    private ChamMonimapMember member;
    
    // 조문
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "LEGISLATION_ARTICLE_ID")
    private LegislationArticle article;
    
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "LEGISLATION_ID")
    private Legislation legislation;
    
    // 부모 댓글 (대댓글용)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PARENT_REPLY_ID")
    private LegislationArticleReply parent;
    
    // 댓글 내용
    @Column(name = "LEGISLATION_ARTICLE_REPLY_CONT")
    private String content;
    
    // 삭제 여부 (0: 정상, 1: 삭제)
    @Column(name = "DEL_STATUS")
    private Boolean delStatus;
    
    public void modifyStatus() {
        this.delStatus = Boolean.TRUE;
    }
    
    public void modify(LegislationArticleReplyPutRequest request) {
        this.content = request.getContent();
    }
    
 
}
