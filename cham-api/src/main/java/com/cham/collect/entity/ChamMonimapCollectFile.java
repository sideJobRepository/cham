package com.cham.collect.entity;

import com.cham.base.BaseData;
import com.cham.collect.enumeration.CollectFileStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 수집기가 받아 S3 에 보관한 원본 첨부 한 개.
 * 행은 수집기가 넣고, cham-api 는 상태만 바꾼다(반영/무시/되돌리기).
 */
@Entity
@Getter
@Table(name = "CHAM_MONIMAP_COLLECT_FILE")
@NoArgsConstructor
public class ChamMonimapCollectFile extends BaseData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CHAM_MONIMAP_COLLECT_FILE_ID")
    private Long chamMonimapCollectFileId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CHAM_MONIMAP_COLLECT_SOURCE_ID")
    private ChamMonimapCollectSource collectSource;

    @Column(name = "CHAM_MONIMAP_COLLECT_JOB_ID")
    private Long chamMonimapCollectJobId;

    // 게시물 제목에서 읽은 대상 연·월. 못 읽으면 null
    @Column(name = "CHAM_MONIMAP_COLLECT_FILE_YEAR")
    private Integer chamMonimapCollectFileYear;

    @Column(name = "CHAM_MONIMAP_COLLECT_FILE_MONTH")
    private Integer chamMonimapCollectFileMonth;

    @Column(name = "CHAM_MONIMAP_COLLECT_FILE_POST_KEY")
    private String chamMonimapCollectFilePostKey;

    @Column(name = "CHAM_MONIMAP_COLLECT_FILE_POST_TITLE")
    private String chamMonimapCollectFilePostTitle;

    @Column(name = "CHAM_MONIMAP_COLLECT_FILE_POST_DATE")
    private LocalDate chamMonimapCollectFilePostDate;

    @Column(name = "CHAM_MONIMAP_COLLECT_FILE_DETAIL_URL")
    private String chamMonimapCollectFileDetailUrl;

    @Column(name = "CHAM_MONIMAP_COLLECT_FILE_ATTACH_KEY")
    private String chamMonimapCollectFileAttachKey;

    @Column(name = "CHAM_MONIMAP_COLLECT_FILE_SOURCE_URL")
    private String chamMonimapCollectFileSourceUrl;

    @Column(name = "CHAM_MONIMAP_COLLECT_FILE_ORIGIN_NAME")
    private String chamMonimapCollectFileOriginName;

    @Column(name = "CHAM_MONIMAP_COLLECT_FILE_EXT")
    private String chamMonimapCollectFileExt;

    @Column(name = "CHAM_MONIMAP_COLLECT_FILE_S3_KEY")
    private String chamMonimapCollectFileS3Key;

    @Column(name = "CHAM_MONIMAP_COLLECT_FILE_SHA256")
    private String chamMonimapCollectFileSha256;

    @Column(name = "CHAM_MONIMAP_COLLECT_FILE_SIZE")
    private Long chamMonimapCollectFileSize;

    @Column(name = "CHAM_MONIMAP_COLLECT_FILE_COLLECTED_AT")
    private LocalDateTime chamMonimapCollectFileCollectedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "CHAM_MONIMAP_COLLECT_FILE_STATUS")
    private CollectFileStatus chamMonimapCollectFileStatus;

    @Column(name = "CHAM_MONIMAP_COLLECT_FILE_DUP_OF_ID")
    private Long chamMonimapCollectFileDupOfId;

    // 반영할 때 쓴 삭제키. 공개관리 목록의 삭제키와 같다
    @Column(name = "CHAM_MONIMAP_COLLECT_FILE_DELKEY")
    private String chamMonimapCollectFileDelkey;

    @Column(name = "CHAM_MONIMAP_COLLECT_FILE_IMPORT_AT")
    private LocalDateTime chamMonimapCollectFileImportAt;

    @Column(name = "CHAM_MONIMAP_COLLECT_FILE_IMPORT_BY")
    private Long chamMonimapCollectFileImportBy;

    @Column(name = "CHAM_MONIMAP_COLLECT_FILE_ERROR")
    private String chamMonimapCollectFileError;

    public void markImported(String deleteKey, Long memberId) {
        this.chamMonimapCollectFileStatus = CollectFileStatus.IMPORTED;
        this.chamMonimapCollectFileDelkey = deleteKey;
        this.chamMonimapCollectFileImportAt = LocalDateTime.now();
        this.chamMonimapCollectFileImportBy = memberId;
        this.chamMonimapCollectFileError = null;
    }

    public void markIgnored() {
        this.chamMonimapCollectFileStatus = CollectFileStatus.IGNORED;
    }

    public void markFailed(String error) {
        this.chamMonimapCollectFileStatus = CollectFileStatus.FAILED;
        this.chamMonimapCollectFileError = error;
    }

    // 검수 대기로 되돌린다. 반영했던 파일이면 삭제키 흔적도 지운다
    public void reset() {
        this.chamMonimapCollectFileStatus = CollectFileStatus.COLLECTED;
        this.chamMonimapCollectFileDelkey = null;
        this.chamMonimapCollectFileImportAt = null;
        this.chamMonimapCollectFileImportBy = null;
        this.chamMonimapCollectFileError = null;
    }
}
