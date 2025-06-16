package com.cham.entity;

import com.cham.entity.base.BaseData;
import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Getter
@Table(name = "REPLY")
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
    @JoinColumn(name = "CARD_USE_ID")
    private CardUse cardUse;
    
    // 댓글 내용
    @Column(name = "REPLY_CONT")
    private String replyCont;
}
