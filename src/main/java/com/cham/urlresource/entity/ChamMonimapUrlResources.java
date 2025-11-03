package com.cham.urlresource.entity;

import com.cham.base.BaseData;
import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Getter
@Table(name = "CHAM_MONIMAP_URL_RESOURCES")
public class ChamMonimapUrlResources extends BaseData {
    
    // 자치연대 예산감시 URL 리소스 ID
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CHAM_MONIMAP_URL_RESOURCES_ID")
    private Long chamMonimapUrlResourcesId;
    
    // 자치연대 예산감시 URL 리소스 경로
    @Column(name = "CHAM_MONIMAP_URL_RESOURCES_PATH")
    private String chamMonimapUrlResourcesPath;
    
    // 자치연대 예산감시 HTTP 메서드
    @Column(name = "CHAM_MONIMAP_HTTP_METHODS")
    private String chamMonimapHttpMethods;
}
