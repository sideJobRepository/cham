package com.cham.collect.repository.impl;

import com.cham.collect.dto.CollectJobResponse;
import com.cham.collect.enumeration.CollectJobStatus;
import com.cham.collect.repository.query.ChamMonimapCollectJobQueryRepository;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;

import java.util.List;

import static com.cham.collect.entity.QChamMonimapCollectJob.chamMonimapCollectJob;
import static com.cham.collect.entity.QChamMonimapCollectSource.chamMonimapCollectSource;
import static com.cham.member.entity.QChamMonimapMember.chamMonimapMember;

@RequiredArgsConstructor
public class ChamMonimapCollectJobRepositoryImpl implements ChamMonimapCollectJobQueryRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public boolean existsActive() {
        Integer one = queryFactory
                .selectOne()
                .from(chamMonimapCollectJob)
                .where(chamMonimapCollectJob.chamMonimapCollectJobStatus.in(
                        CollectJobStatus.REQUESTED, CollectJobStatus.RUNNING))
                .fetchFirst();
        return one != null;
    }

    @Override
    public Page<CollectJobResponse> findJobs(Pageable pageable) {
        List<CollectJobResponse> content = queryFactory
                .select(Projections.constructor(CollectJobResponse.class,
                        chamMonimapCollectJob.chamMonimapCollectJobId,
                        chamMonimapCollectJob.chamMonimapCollectJobTrigger,
                        chamMonimapCollectJob.chamMonimapCollectJobRequestedBy,
                        chamMonimapMember.chamMonimapMemberName,
                        chamMonimapCollectSource.chamMonimapCollectSourceId,
                        chamMonimapCollectSource.chamMonimapCollectSourceName,
                        chamMonimapCollectJob.chamMonimapCollectJobTargetYear,
                        chamMonimapCollectJob.chamMonimapCollectJobTargetMonth,
                        chamMonimapCollectJob.chamMonimapCollectJobStatus,
                        chamMonimapCollectJob.chamMonimapCollectJobStartedAt,
                        chamMonimapCollectJob.chamMonimapCollectJobFinishedAt,
                        chamMonimapCollectJob.chamMonimapCollectJobPostsSeen,
                        chamMonimapCollectJob.chamMonimapCollectJobFilesNew,
                        chamMonimapCollectJob.chamMonimapCollectJobFilesSkipped,
                        chamMonimapCollectJob.chamMonimapCollectJobFilesFailed,
                        chamMonimapCollectJob.chamMonimapCollectJobLog,
                        chamMonimapCollectJob.registDate))
                .from(chamMonimapCollectJob)
                .leftJoin(chamMonimapCollectJob.collectSource, chamMonimapCollectSource)
                .leftJoin(chamMonimapMember)
                .on(chamMonimapMember.chamMonimapMemberId.eq(chamMonimapCollectJob.chamMonimapCollectJobRequestedBy))
                .orderBy(chamMonimapCollectJob.chamMonimapCollectJobId.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> count = queryFactory
                .select(chamMonimapCollectJob.count())
                .from(chamMonimapCollectJob);

        return PageableExecutionUtils.getPage(content, pageable, count::fetchOne);
    }
}
