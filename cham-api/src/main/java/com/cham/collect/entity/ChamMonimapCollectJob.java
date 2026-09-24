package com.cham.collect.entity;

import com.cham.base.BaseData;
import com.cham.collect.enumeration.CollectJobStatus;
import com.cham.collect.enumeration.CollectJobTrigger;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 수집 실행 한 번. 수집기의 작업 이력이자 관리자 요청 대기열이다.
 * cham-api 는 REQUESTED 행을 넣기만 하고, 실행과 결과 기록은 수집기가 한다.
 */
@Entity
@Getter
@Table(name = "CHAM_MONIMAP_COLLECT_JOB")
@NoArgsConstructor
public class ChamMonimapCollectJob extends BaseData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CHAM_MONIMAP_COLLECT_JOB_ID")
    private Long chamMonimapCollectJobId;

    @Enumerated(EnumType.STRING)
    @Column(name = "CHAM_MONIMAP_COLLECT_JOB_TRIGGER")
    private CollectJobTrigger chamMonimapCollectJobTrigger;

    @Column(name = "CHAM_MONIMAP_COLLECT_JOB_REQUESTED_BY")
    private Long chamMonimapCollectJobRequestedBy;

    // null 이면 사용 중인 기관 전체
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CHAM_MONIMAP_COLLECT_SOURCE_ID")
    private ChamMonimapCollectSource collectSource;

    @Column(name = "CHAM_MONIMAP_COLLECT_JOB_PAGE_LIMIT")
    private Integer chamMonimapCollectJobPageLimit;

    // 달을 골라 요청한 경우. 수집기는 이 달 게시물만 받는다
    @Column(name = "CHAM_MONIMAP_COLLECT_JOB_TARGET_YEAR")
    private Integer chamMonimapCollectJobTargetYear;

    @Column(name = "CHAM_MONIMAP_COLLECT_JOB_TARGET_MONTH")
    private Integer chamMonimapCollectJobTargetMonth;

    @Enumerated(EnumType.STRING)
    @Column(name = "CHAM_MONIMAP_COLLECT_JOB_STATUS")
    private CollectJobStatus chamMonimapCollectJobStatus;

    @Column(name = "CHAM_MONIMAP_COLLECT_JOB_STARTED_AT")
    private LocalDateTime chamMonimapCollectJobStartedAt;

    @Column(name = "CHAM_MONIMAP_COLLECT_JOB_FINISHED_AT")
    private LocalDateTime chamMonimapCollectJobFinishedAt;

    @Column(name = "CHAM_MONIMAP_COLLECT_JOB_POSTS_SEEN")
    private Integer chamMonimapCollectJobPostsSeen;

    @Column(name = "CHAM_MONIMAP_COLLECT_JOB_FILES_NEW")
    private Integer chamMonimapCollectJobFilesNew;

    @Column(name = "CHAM_MONIMAP_COLLECT_JOB_FILES_SKIPPED")
    private Integer chamMonimapCollectJobFilesSkipped;

    @Column(name = "CHAM_MONIMAP_COLLECT_JOB_FILES_FAILED")
    private Integer chamMonimapCollectJobFilesFailed;

    @Column(name = "CHAM_MONIMAP_COLLECT_JOB_LOG")
    private String chamMonimapCollectJobLog;

    public static ChamMonimapCollectJob manual(Long memberId, ChamMonimapCollectSource source,
                                               Integer targetYear, Integer targetMonth) {
        ChamMonimapCollectJob job = new ChamMonimapCollectJob();
        job.chamMonimapCollectJobTrigger = CollectJobTrigger.MANUAL;
        job.chamMonimapCollectJobRequestedBy = memberId;
        job.collectSource = source;
        job.chamMonimapCollectJobTargetYear = targetYear;
        job.chamMonimapCollectJobTargetMonth = targetMonth;
        job.chamMonimapCollectJobStatus = CollectJobStatus.REQUESTED;
        job.chamMonimapCollectJobPostsSeen = 0;
        job.chamMonimapCollectJobFilesNew = 0;
        job.chamMonimapCollectJobFilesSkipped = 0;
        job.chamMonimapCollectJobFilesFailed = 0;
        return job;
    }
}
