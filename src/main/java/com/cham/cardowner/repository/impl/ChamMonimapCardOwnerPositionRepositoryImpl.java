package com.cham.cardowner.repository.impl;

import com.cham.cardowner.entity.ChamMonimapCardOwnerPosition;
import com.cham.cardowner.repository.query.ChamMonimapCardOwnerPositionQueryRepository;
import com.cham.dto.response.CardOwnerPositionDto;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;

import static com.cham.cardowner.entity.QChamMonimapCardOwnerPosition.chamMonimapCardOwnerPosition;


@RequiredArgsConstructor
public class ChamMonimapCardOwnerPositionRepositoryImpl implements ChamMonimapCardOwnerPositionQueryRepository {
    
    private final JPAQueryFactory queryFactory;
    
    @Override
    public Optional<ChamMonimapCardOwnerPosition> findByCardOwnerPositionName(String name) {
        ChamMonimapCardOwnerPosition result = queryFactory
                .selectFrom(chamMonimapCardOwnerPosition)
                .where(chamMonimapCardOwnerPosition.chamMonimapCardOwnerPositionName.eq(name))
                .fetchOne();
        return Optional.ofNullable(result);
    }
    
    @Override
    public List<CardOwnerPositionDto> findByCardOwnerPositionDtos() {
        return queryFactory
                .select(Projections.constructor(
                        CardOwnerPositionDto.class,
                        chamMonimapCardOwnerPosition.chamMonimapCardOwnerPositionId,
                        chamMonimapCardOwnerPosition.chamMonimapCardOwnerPositionName
                ))
                .from(chamMonimapCardOwnerPosition)
                .fetch();
    }
}
