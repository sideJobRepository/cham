package com.cham.entity;

import com.cham.controller.request.ReplyModifyRequest;
import com.cham.entity.base.BaseData;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "CHAM_MONIMAP_REPLY")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChamMonimapReply extends BaseData {
    
    // 댓글 ID
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CHAM_MONIMAP_REPLY_ID")
    private Long chamMonimapReplyId;
    
    // 회원 ID
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CHAM_MONIMAP_MEMBER_ID")
    private ChamMonimapMember chamMonimapMember;
    
    // 카드 사용 ID
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CHAM_MONIMAP_CARD_USE_ADDR_ID")
    private ChamMonimapCardUseAddr chamMonimapCardUseAddr;
    
    // 댓글 내용
    @Column(name = "CHAM_MONIMAP_REPLY_CONT")
    private String chamMonimapReplyCont;
    
    public ChamMonimapReply(Long memberId, Long cardUseAddrId, String replyCont) {
        ChamMonimapMember member = new ChamMonimapMember(memberId);
        ChamMonimapCardUseAddr cardUseAddr = new ChamMonimapCardUseAddr(cardUseAddrId);
        this.chamMonimapMember = member;
        this.chamMonimapCardUseAddr = cardUseAddr;
        this.chamMonimapReplyCont = replyCont;
    }
    
    public ChamMonimapReply(Long replyId) {
        this.chamMonimapReplyId = replyId;
    }
    
    
    public void modifyReply(ReplyModifyRequest request) {
        this.chamMonimapReplyCont = request.getReplyCont();
    }
    
}
