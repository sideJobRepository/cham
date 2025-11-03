package com.cham.file.entity;

import com.cham.base.BaseData;
import com.cham.file.enumeration.ChamMonimapFileType;
import jakarta.persistence.*;
import lombok.*;

@Table(name = "CHAM_MONIMAP_COMMON_FILE")
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ChamMonimapCommonFile extends BaseData {

    
    // 자치연대 예산감시 공통 파일 ID
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CHAM_MONIMAP_COMMON_FILE_ID")
    private Long id;

    // 자치연대 예산감시 공통 파일 타겟 ID
    @Column(name = "CHAM_MONIMAP_COMMON_FILE_TARGET_ID")
    private Long targetId;

    // 자치연대 예산감시 공통 파일 이름
    @Column(name = "CHAM_MONIMAP_COMMON_FILE_NAME")
    private String fileName;

    // 자치연대 예산감시 공통 파일 UUID 이름
    @Column(name = "CHAM_MONIMAP_COMMON_FILE_UUID_NAME")
    private String uuidName;

    // 자치연대 예산감시 공통 파일 타입
    @Column(name = "CHAM_MONIMAP_COMMON_FILE_TYPE")
    @Enumerated(EnumType.STRING)
    private ChamMonimapFileType fileType;

    // 자치연대 예산감시 공통 파일 URL
    @Column(name = "CHAM_MONIMAP_COMMON_FILE_URL")
    private String fileUrl;
}
