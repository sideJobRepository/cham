package com.cham.feedbacck.legislationarticle.entity;

import com.cham.base.BaseData;
import com.cham.feedbacck.legislation.entity.Legislation;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Table(name = "LEGISLATION_ARTICLE")
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
public class LegislationArticle extends BaseData {
    
    // 법률제정 조항 ID
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "LEGISLATION_ARTICLE_ID")
    private Long id;
    
    // 법률제정 ID
    @JoinColumn(name = "LEGISLATION_ID")
    @ManyToOne(fetch = FetchType.LAZY)
    private Legislation legislation;
    
    // 파트
    @Column(name = "PART")
    private String part;
    
    @Column(name = "CHAPTER")
    private String chapter;
    
    // 섹션
    @Column(name = "SECTION")
    private String section;
    
    // 조항 번호
    @Column(name = "ARTICLE_NO")
    private String articleNo;
    
    // 조항 제목
    @Column(name = "ARTICLE_TITLE")
    private String articleTitle;
    
    // 내용
    @Column(name = "CONT",columnDefinition = "TEXT")
    private String cont;
    
    // 카테고리 메인
    @Column(name = "CATEGORY_MAIN")
    private String categoryMain;
    
    // 카테고리 SUB
    @Column(name = "CATEGORY_SUB")
    private String categorySub;
    
    // 정렬 번호
    @Column(name = "ORDERS_NO")
    private Integer ordersNo;
    
}
