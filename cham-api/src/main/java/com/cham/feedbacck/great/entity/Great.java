package com.cham.feedbacck.great.entity;


import com.cham.base.BaseData;
import com.cham.feedbacck.great.enums.GreatType;
import com.cham.feedbacck.legislationarticle.entity.LegislationArticle;
import com.cham.member.entity.ChamMonimapMember;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Table(name = "LEGISLATION_ARTICLE_GREAT")
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
public class Great extends BaseData {
    
    // 법률제정 조항 좋아요 ID
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "LEGISLATION_ARTICLE_GREAT_ID")
    private Long id;
    
    // 자치연대 예산감시 회원 ID
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CHAM_MONIMAP_MEMBER_ID")
    private ChamMonimapMember member;
    
    // 법률제정 조항 ID
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "LEGISLATION_ARTICLE_ID")
    private LegislationArticle legislationArticle;
    
    // 법률제정 조항 좋아요 타입
    @Column(name = "LEGISLATION_ARTICLE_GREAT_TYPE")
    @Enumerated(EnumType.STRING)
    private GreatType greatType;
    
    public void modify(GreatType greatType) {
        this.greatType = greatType;
    }
}
