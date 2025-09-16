package com.cham.replyimage.entity;

import com.base.BaseData;
import com.cham.reply.entity.ChamMonimapReply;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "CHAM_MONIMAP_REPLY_IMAGE")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChamMonimapReplyImage extends BaseData {
    
    
    // 댓글 이미지 ID
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CHAM_MONIMAP_REPLY_IMAGE_ID")
    private Long chamMonimapReplyImageId;
    
    // 댓글 ID
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CHAM_MONIMAP_REPLY_ID")
    private ChamMonimapReply chamMonimapReply;
    
    // 댓글 이미지 URL
    @Column(name = "CHAM_MONIMAP_REPLY_IMAGE_URL")
    private String chamMonimapReplyImageUrl;
    
    public ChamMonimapReplyImage(ChamMonimapReply reply, String replyImageUrl) {
        this.chamMonimapReply = reply;
        this.chamMonimapReplyImageUrl = replyImageUrl;
    }
}
