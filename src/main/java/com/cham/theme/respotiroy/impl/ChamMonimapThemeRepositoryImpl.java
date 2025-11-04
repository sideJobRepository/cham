package com.cham.theme.respotiroy.impl;

import com.cham.theme.dto.response.QThemeGetResponse;
import com.cham.theme.dto.response.ThemeGetResponse;
import com.cham.theme.entity.ChamMonimapTheme;
import com.cham.theme.respotiroy.query.ChamMonimapThemeQueryRepository;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.List;

import static com.cham.cardowner.entity.QChamMonimapCardOwnerPosition.chamMonimapCardOwnerPosition;
import static com.cham.file.entity.QChamMonimapCommonFile.chamMonimapCommonFile;
import static com.cham.theme.entity.QChamMonimapTheme.chamMonimapTheme;

@RequiredArgsConstructor
public class ChamMonimapThemeRepositoryImpl implements ChamMonimapThemeQueryRepository {

    private final JPAQueryFactory queryFactory;
    
    @Override
    public List<ThemeGetResponse> findByThemes() {
        return  queryFactory
                .select(
                        new QThemeGetResponse(
                                chamMonimapTheme.id,
                                chamMonimapTheme.targetId,
                                chamMonimapTheme.color,
                                chamMonimapTheme.type.stringValue(),
                                chamMonimapTheme.inputValue,
                                chamMonimapCardOwnerPosition.chamMonimapCardOwnerPositionName,
                                chamMonimapCommonFile.fileUrl,
                                chamMonimapCommonFile.fileName
                        )
                )
                .from(chamMonimapTheme)
                .leftJoin(chamMonimapCardOwnerPosition)
                .on(chamMonimapTheme.targetId.eq(chamMonimapCardOwnerPosition.chamMonimapCardOwnerPositionId))
                .leftJoin(chamMonimapCommonFile)
                .on(chamMonimapTheme.id.eq(chamMonimapCommonFile.targetId))
                .fetch();
    }
    
    @Override
    public List<Long> findByDuplicationCheckTargetId(List<Long> targetIds) {
        return queryFactory
                .select(chamMonimapTheme.targetId)
                .from(chamMonimapTheme)
                .where(chamMonimapTheme.targetId.in(targetIds))
                .fetch();
    }
    
    @Override
    public List<ChamMonimapTheme> findByThemeIdsIn(List<Long> ids) {
        return queryFactory
                .selectFrom(chamMonimapTheme)
                .where(chamMonimapTheme.id.in(ids))
                .fetch();
    }
}
