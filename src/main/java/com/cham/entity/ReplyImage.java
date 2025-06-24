package com.cham.entity;

import com.cham.entity.base.BaseData;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "REPLY_IMAGE")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReplyImage extends BaseData {
    
    
    // 댓글 이미지 ID
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "REPLY_IMAGE_ID")
    private Long replyImageId;
    
    // 댓글 ID
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "REPLY_ID")
    private Reply reply;
    
    // 댓글 이미지 URL
    @Column(name = "REPLY_IMAGE_URL")
    private String replyImageUrl;
    
    public ReplyImage(Reply reply, String replyImageUrl) {
        this.reply = reply;
        this.replyImageUrl = replyImageUrl;
    }
}
