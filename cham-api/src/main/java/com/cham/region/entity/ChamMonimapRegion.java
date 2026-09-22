package com.cham.region.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "CHAM_MONIMAP_REGION")
@Getter
@NoArgsConstructor
public class ChamMonimapRegion {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CHAM_MONIMAP_REGION_ID")
    private Long chamMonimapRegionId;
    
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CHAM_MONIMAP_PARENT_REGION_ID")
    private ChamMonimapRegion parent;
    
    @Column(name = "CHAM_MONIMAP_REGION_NAME")
    private String chamMonimapRegionName;
    
    @Column(name = "CHAM_MONIMAP_REGION_TYPE")
    private String chamMonimapRegionType;
    
    @Column(name = "CHAM_MONIMAP_REGION_DEPTH")
    private Integer chamMonimapRegionDepth;
    
    @Column(name = "CHAM_MONIMAP_REGION_X-VALUE")
    private String chamMonimapRegionXValue;
    
    @Column(name = "CHAM_MONIMAP_REGION_Y-VALUE")
    private String chamMonimapRegionYValue;
    
    public ChamMonimapRegion(ChamMonimapRegion parent, String chamMonimapRegionName, String chamMonimapRegionType, Integer chamMonimapRegionDepth, String chamMonimapRegionXValue, String chamMonimapRegionYValue) {
        this.parent = parent;
        this.chamMonimapRegionName = chamMonimapRegionName;
        this.chamMonimapRegionType = chamMonimapRegionType;
        this.chamMonimapRegionDepth = chamMonimapRegionDepth;
        this.chamMonimapRegionXValue = chamMonimapRegionXValue;
        this.chamMonimapRegionYValue = chamMonimapRegionYValue;
    }
}
