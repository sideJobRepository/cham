package com.cham.entity;

import com.cham.controller.request.ReplyModifyRequest;
import com.cham.entity.base.BaseData;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicUpdate;

import java.util.Objects;

@Entity
@Getter
@Table(name = "REPLY")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Reply extends BaseData {
    
    // 댓글 ID
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "REPLY_ID")
    private Long replyId;
    
    // 회원 ID
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MEMBER_ID")
    private Member member;
    
    // 카드 사용 ID
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CARD_USE_ADDR_ID")
    private CardUseAddr cardUseAddr;
    
    // 댓글 내용
    @Column(name = "REPLY_CONT")
    private String replyCont;
    
    public Reply(Long memberId, Long cardUseAddrId, String replyCont) {
        Member member = new Member(memberId);
        CardUseAddr cardUseAddr = new CardUseAddr(cardUseAddrId);
        this.member = member;
        this.cardUseAddr = cardUseAddr;
        this.replyCont = replyCont;
    }
    
    public Reply(Long replyId) {
        this.replyId = replyId;
    }
    
    
    public void modifyReply(ReplyModifyRequest request) {
        this.replyCont = request.getReplyCont();
    }
    
}
