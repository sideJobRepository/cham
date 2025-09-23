package com.cham.check.entity;

import com.base.BaseData;
import com.cham.carduseaddr.entity.ChamMonimapCardUseAddr;
import com.cham.member.entity.ChamMonimapMember;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "CHAM_MONIMAP_CHECK_LOG")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class ChamMonimapCheckLog  extends BaseData {
    
    
    // 자치연대 예산감시 체크 기록 ID
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CHAM_MONIMAP_CHECK_LOG_ID")
    private Long chamMonimapCheckLogId;
    
    // 자치연대 예산감시 회원 ID
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CHAM_MONIMAP_MEMBER_ID")
    private ChamMonimapMember chamMonimapMember;
    
    // 자치연대 예산감시 카드 사용 장소 ID
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CHAM_MONIMAP_CARD_USE_ADDR_ID")
    private ChamMonimapCardUseAddr chamMonimapCardUseAddr;
    
    // 자치연대 예산감시 방문여부
    @Column(name = "CHAM_MONIMAP_VISITED")
    private String chamMonimapVisited;
    
    // 자치연대 예산감시 의심여부
    @Column(name = "CHAM_MONIMAP_SUSPICIOUSED")
    private String chamMonimapSuspicioused;
    
    public ChamMonimapCheckLog(ChamMonimapMember chamMonimapMember, ChamMonimapCardUseAddr chamMonimapCardUseAddr, String chamMonimapVisited, String chamMonimapSuspicioused) {
        this.chamMonimapMember = chamMonimapMember;
        this.chamMonimapCardUseAddr = chamMonimapCardUseAddr;
        this.chamMonimapVisited = chamMonimapVisited;
        this.chamMonimapSuspicioused = chamMonimapSuspicioused;
    }
    
    public void modifyVisited(String visited) {
        this.chamMonimapVisited = visited;
    }
    public void modifySuspicioused(String suspicioused) {
        this.chamMonimapSuspicioused = suspicioused;
    }
}
