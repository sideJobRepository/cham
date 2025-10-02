package com.cham.caruse.dto;

import com.cham.region.entity.ChamMonimapRegion;
import lombok.*;

import java.util.ArrayDeque;
import java.util.Deque;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegionSummaryDto {
    private Long   regionId;   // 해당 레벨의 REGION_ID (city/gu/dong 중 하나)
    private Long parentId; //부모 ID 추가
    private int    depth;      // 0=시/도, 1=구/군, 2=동
    private String path;       // "대전", "대전 서구", "대전 동구 신하동"
    private String x;          // 해당 레벨(region)의 X
    private String y;          // 해당 레벨(region)의 Y
    private int    count;      // 총 건수(해당 레벨에 귀속된 카드사용 수)
    
    public void inc() { this.count++; }
    
    
    public static String buildPath(ChamMonimapRegion r) {
        Deque<String> parts = new ArrayDeque<>();
        while (r != null) {
            parts.addFirst(r.getChamMonimapRegionName());
            r = r.getParent();
        }
        return String.join(" ", parts);
    }
    
    public static RegionSummaryDto toSummarySkeleton(ChamMonimapRegion r) {
        return RegionSummaryDto.builder()
                .regionId(r.getChamMonimapRegionId())
                .parentId(r.getParent() != null ? r.getParent().getChamMonimapRegionId() : null)
                .depth(r.getChamMonimapRegionDepth())
                .path(buildPath(r))
                .x(r.getChamMonimapRegionXValue())
                .y(r.getChamMonimapRegionYValue())
                .count(0)
                .build();
    }
    
}
