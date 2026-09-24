package com.cham.collect.repository.impl;

import com.cham.collect.dto.CollectFileCell;
import com.cham.collect.dto.CollectFileResponse;
import com.cham.collect.enumeration.CollectFileStatus;
import com.cham.collect.repository.query.ChamMonimapCollectFileQueryRepository;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;

import java.util.List;

import static com.cham.collect.entity.QChamMonimapCollectFile.chamMonimapCollectFile;
import static com.cham.collect.entity.QChamMonimapCollectSource.chamMonimapCollectSource;

@RequiredArgsConstructor
public class ChamMonimapCollectFileRepositoryImpl implements ChamMonimapCollectFileQueryRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<CollectFileResponse> findFiles(Long sourceId, Integer year, Integer month, CollectFileStatus status,
                                               Pageable pageable) {
        List<CollectFileResponse> content = queryFactory
                .select(Projections.constructor(CollectFileResponse.class,
                        chamMonimapCollectFile.chamMonimapCollectFileId,
                        chamMonimapCollectSource.chamMonimapCollectSourceId,
                        chamMonimapCollectSource.chamMonimapCollectSourceName,
                        chamMonimapCollectFile.chamMonimapCollectFileYear,
                        chamMonimapCollectFile.chamMonimapCollectFileMonth,
                        chamMonimapCollectFile.chamMonimapCollectFilePostTitle,
                        chamMonimapCollectFile.chamMonimapCollectFilePostDate,
                        chamMonimapCollectFile.chamMonimapCollectFileDetailUrl,
                        chamMonimapCollectFile.chamMonimapCollectFileOriginName,
                        chamMonimapCollectFile.chamMonimapCollectFileExt,
                        chamMonimapCollectFile.chamMonimapCollectFileSize,
                        chamMonimapCollectFile.chamMonimapCollectFileCollectedAt,
                        chamMonimapCollectFile.chamMonimapCollectFileStatus,
                        chamMonimapCollectFile.chamMonimapCollectFileDelkey,
                        chamMonimapCollectFile.chamMonimapCollectFileImportAt,
                        chamMonimapCollectFile.chamMonimapCollectFileError))
                .from(chamMonimapCollectFile)
                .join(chamMonimapCollectFile.collectSource, chamMonimapCollectSource)
                .where(sourceEq(sourceId), yearEq(year), monthEq(month), statusEq(status))
                .orderBy(chamMonimapCollectFile.chamMonimapCollectFileCollectedAt.desc(),
                        chamMonimapCollectFile.chamMonimapCollectFileId.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> count = queryFactory
                .select(chamMonimapCollectFile.count())
                .from(chamMonimapCollectFile)
                .where(sourceEq(sourceId), yearEq(year), monthEq(month), statusEq(status));

        return PageableExecutionUtils.getPage(content, pageable, count::fetchOne);
    }

    @Override
    public List<CollectFileCell> findYearCells(int year) {
        return queryFactory
                .select(Projections.constructor(CollectFileCell.class,
                        chamMonimapCollectFile.chamMonimapCollectFileId,
                        chamMonimapCollectFile.collectSource.chamMonimapCollectSourceId,
                        chamMonimapCollectFile.chamMonimapCollectFileMonth,
                        chamMonimapCollectFile.chamMonimapCollectFileStatus,
                        chamMonimapCollectFile.chamMonimapCollectFileCollectedAt))
                .from(chamMonimapCollectFile)
                .where(chamMonimapCollectFile.chamMonimapCollectFileYear.eq(year),
                        chamMonimapCollectFile.chamMonimapCollectFileMonth.isNotNull(),
                        chamMonimapCollectFile.chamMonimapCollectFileStatus.ne(CollectFileStatus.DUPLICATE))
                .orderBy(chamMonimapCollectFile.chamMonimapCollectFileCollectedAt.desc(),
                        chamMonimapCollectFile.chamMonimapCollectFileId.desc())
                .fetch();
    }

    private BooleanExpression sourceEq(Long sourceId) {
        return sourceId == null ? null : chamMonimapCollectFile.collectSource.chamMonimapCollectSourceId.eq(sourceId);
    }

    private BooleanExpression yearEq(Integer year) {
        return year == null ? null : chamMonimapCollectFile.chamMonimapCollectFileYear.eq(year);
    }

    private BooleanExpression monthEq(Integer month) {
        return month == null ? null : chamMonimapCollectFile.chamMonimapCollectFileMonth.eq(month);
    }

    private BooleanExpression statusEq(CollectFileStatus status) {
        return status == null ? null : chamMonimapCollectFile.chamMonimapCollectFileStatus.eq(status);
    }
}
