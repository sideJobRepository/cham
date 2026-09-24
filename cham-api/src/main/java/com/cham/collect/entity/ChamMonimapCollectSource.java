package com.cham.collect.entity;

import com.cham.base.BaseData;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 수집 출처(기관). 행은 수집ddl.sql 시드로 넣는다. cham-api 는 읽기만 한다.
 */
@Entity
@Getter
@Table(name = "CHAM_MONIMAP_COLLECT_SOURCE")
@NoArgsConstructor
public class ChamMonimapCollectSource extends BaseData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CHAM_MONIMAP_COLLECT_SOURCE_ID")
    private Long chamMonimapCollectSourceId;

    // S3 경로에 쓰는 코드 (JUNGGU_COUNCIL)
    @Column(name = "CHAM_MONIMAP_COLLECT_SOURCE_CODE")
    private String chamMonimapCollectSourceCode;

    @Column(name = "CHAM_MONIMAP_COLLECT_SOURCE_NAME")
    private String chamMonimapCollectSourceName;

    @Column(name = "CHAM_MONIMAP_COLLECT_SOURCE_ENGINE")
    private String chamMonimapCollectSourceEngine;

    @Column(name = "CHAM_MONIMAP_COLLECT_SOURCE_LIST_URL")
    private String chamMonimapCollectSourceListUrl;

    // 반영할 때 직위(분류). CARD_OWNER_POSITION_NAME 에 있는 값이어야 한다
    @Column(name = "CHAM_MONIMAP_COLLECT_SOURCE_POSITION_NAME")
    private String chamMonimapCollectSourcePositionName;

    @Column(name = "CHAM_MONIMAP_COLLECT_SOURCE_REGION")
    private String chamMonimapCollectSourceRegion;

    // 엑셀 2열 '사용자'(직함) 기본값. 원본에 사용자 칸이 없고 시트 이름도 직함이 아닐 때 쓴다 (구청장)
    @Column(name = "CHAM_MONIMAP_COLLECT_SOURCE_DEFAULT_USER")
    private String chamMonimapCollectSourceDefaultUser;

    // 엑셀 3열 '이름' 기본값. 단체장 이름. 의회는 비어 있다
    @Column(name = "CHAM_MONIMAP_COLLECT_SOURCE_DEFAULT_NAME")
    private String chamMonimapCollectSourceDefaultName;

    @Column(name = "CHAM_MONIMAP_COLLECT_SOURCE_FILE_FORMAT")
    private String chamMonimapCollectSourceFileFormat;

    @Column(name = "CHAM_MONIMAP_COLLECT_SOURCE_ENABLED")
    private Boolean chamMonimapCollectSourceEnabled;

    @Column(name = "CHAM_MONIMAP_COLLECT_SOURCE_SORT")
    private Integer chamMonimapCollectSourceSort;

    @Column(name = "CHAM_MONIMAP_COLLECT_SOURCE_LAST_SUCCESS")
    private LocalDateTime chamMonimapCollectSourceLastSuccess;

    @Column(name = "CHAM_MONIMAP_COLLECT_SOURCE_LAST_ERROR")
    private String chamMonimapCollectSourceLastError;

    @Column(name = "CHAM_MONIMAP_COLLECT_SOURCE_NOTE")
    private String chamMonimapCollectSourceNote;
}
