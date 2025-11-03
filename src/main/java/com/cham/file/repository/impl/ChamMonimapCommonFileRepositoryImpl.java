package com.cham.file.repository.impl;

import com.cham.file.entity.ChamMonimapCommonFile;
import com.cham.file.enumeration.ChamMonimapFileType;
import com.cham.file.repository.query.ChamMonimapCommonFileQueryRepository;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.List;

import static com.cham.file.entity.QChamMonimapCommonFile.chamMonimapCommonFile;

@RequiredArgsConstructor
public class ChamMonimapCommonFileRepositoryImpl implements ChamMonimapCommonFileQueryRepository {
    
    private final JPAQueryFactory queryFactory;
    
    @Override
    public List<ChamMonimapCommonFile> findTargetIds(Long targetId) {
        
        return queryFactory
                .selectFrom(chamMonimapCommonFile)
                .where(chamMonimapCommonFile.targetId.in(targetId),
                        chamMonimapCommonFile.fileType.eq(ChamMonimapFileType.THEME)
                        ).fetch();
    }
}
