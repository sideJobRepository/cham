package com.cham.caruse.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class KakaoPlaceResponse {
    
    private Meta meta;
    private List<Document> documents;
    
    @Data
    public static class Meta {
        @JsonProperty("same_name")
        private SameName sameName;
        
        @JsonProperty("pageable_count")
        private int pageableCount;
        
        @JsonProperty("total_count")
        private int totalCount;
        
        @JsonProperty("is_end")
        private boolean isEnd;
    }
    
    @Data
    public static class SameName {
        private List<String> region;       // region: []
        private String keyword;            // "카카오프렌즈"
        @JsonProperty("selected_region")
        private String selectedRegion;     // ""
    }
    
    @Data
    public static class Document {
        @JsonProperty("place_name")
        private String placeName;
        
        private String distance;
        
        @JsonProperty("place_url")
        private String placeUrl;
        
        @JsonProperty("category_name")
        private String categoryName;
        
        @JsonProperty("address_name")
        private String addressName;
        
        @JsonProperty("road_address_name")
        private String roadAddressName;
        
        private String id;
        private String phone;
        
        @JsonProperty("category_group_code")
        private String categoryGroupCode;
        
        @JsonProperty("category_group_name")
        private String categoryGroupName;
        
        private String x;   // 경도
        private String y;   // 위도
    }
}