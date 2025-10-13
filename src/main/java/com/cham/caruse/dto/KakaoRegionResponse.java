package com.cham.caruse.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class KakaoRegionResponse {
    
    private List<Document> documents;
    private Meta meta;
    
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Meta {
        private int total_count;
    }
    
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Document {
        private String region_type;         // H: 행정동, B: 법정동
        private String address_name;        // 전체 주소명
        private String region_1depth_name;  // 시/도
        private String region_2depth_name;  // 시/군/구
        private String region_3depth_name;  // 읍/면/동
        private String region_4depth_name;  // 리 단위 (있을 때만)
        private String code;                // 행정코드
        private String x;                   // 경도
        private String y;                   // 위도
        
        // 접근 편의용 게터
        public String getRegion1depthName() {
            return region_1depth_name;
        }
        
        public String getRegion2depthName() {
            return region_2depth_name;
        }
        
        public String getRegion3depthName() {
            return region_3depth_name;
        }
    }
}