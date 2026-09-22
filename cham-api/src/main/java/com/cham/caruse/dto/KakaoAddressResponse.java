package com.cham.caruse.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;
@Data
public class KakaoAddressResponse {
    private Meta meta;
    private List<Document> documents;
    
    public KakaoAddressResponse() {
        this.meta = new Meta();
        this.documents = new ArrayList<>();
    }
    
    @Data
    public static class Meta {
        private int total_count;
        private int pageable_count;
        private boolean is_end;
    }
    
    @Data
    public static class Document {
        private String address_name;
        private String address_type;
        private String x;
        private String y;
        
        private Address address;
        private RoadAddress road_address;
    }
    
    @Data
    public static class Address {
        private String address_name;
        private String region_1depth_name;
        private String region_2depth_name;
        private String region_3depth_name;
        private String region_3depth_h_name;
        private String h_code;
        private String b_code;
        private String mountain_yn;
        private String main_address_no;
        private String sub_address_no;
        private String x;
        private String y;
    }
    
    @Data
    public static class RoadAddress {
        private String address_name;
        private String region_1depth_name;
        private String region_2depth_name;
        private String region_3depth_name;
        private String road_name;
        private String underground_yn;
        private String main_building_no;
        private String sub_building_no;
        private String building_name;
        private String zone_no;
        private String x;
        private String y;
    }
}
