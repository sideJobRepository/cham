package com.cham.carduseaddr.repository.impl;

import com.cham.carduseaddr.entity.ChamMonimapCardUseAddr;
import com.cham.carduseaddr.repository.query.ChamMonimapCardUseAddrQueryRepository;
import com.cham.dto.response.CardUseAddrDto;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.cham.carduseaddr.entity.QChamMonimapCardUseAddr.chamMonimapCardUseAddr;


@RequiredArgsConstructor
@Repository
public class ChamMonimapCardUseAddrRepositoryImpl implements ChamMonimapCardUseAddrQueryRepository {
    
    private final JPAQueryFactory queryFactory;
    
    @Override
    public String findByImageUrl(Long cardUseAddrId) {
          return queryFactory.select(chamMonimapCardUseAddr.chamMonimapCardUseImageUrl)
                .from(chamMonimapCardUseAddr)
                .where(chamMonimapCardUseAddr.chamMonimapCardUseAddrId.eq(cardUseAddrId))
                .fetchOne();
    }
    
    @Override
    public List<CardUseAddrDto> findByCardUseAddrDtos() {
        return queryFactory
                .select(Projections.constructor(
                        CardUseAddrDto.class,
                        chamMonimapCardUseAddr.chamMonimapCardUseAddrId,
                        chamMonimapCardUseAddr.chamMonimapCardUseAddrName,
                        chamMonimapCardUseAddr.chamMonimapCardUseDetailAddr
                ))
                .from(chamMonimapCardUseAddr)
                .fetch();
    }
    
    @Override
    public List<ChamMonimapCardUseAddr> findImageUrlsByAddrIds(Set<Long> addrIds) {
        return queryFactory
                .selectFrom(chamMonimapCardUseAddr)
                .where(chamMonimapCardUseAddr.chamMonimapCardUseAddrId.in(addrIds))
                .fetch();
    }
    @Override
    public Optional<ChamMonimapCardUseAddr> findByXValueAndYValue(String x, String y,double range) {
        double xd, yd;
        try {
            xd = Double.parseDouble(x);
            yd = Double.parseDouble(y);
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
        
        // 문자열 컬럼을 double로 캐스팅
        var xNum = Expressions.numberTemplate(Double.class,
                "CAST({0} AS DOUBLE)", chamMonimapCardUseAddr.chamMonimapCardUseXValue);
        var yNum = Expressions.numberTemplate(Double.class,
                "CAST({0} AS DOUBLE)", chamMonimapCardUseAddr.chamMonimapCardUseYValue);
        
        ChamMonimapCardUseAddr result = queryFactory
                .selectFrom(chamMonimapCardUseAddr)
                .where(
                        xNum.between(xd - range, xd + range),
                        yNum.between(yd - range, yd + range)
                )
                .fetchFirst();
        
        return Optional.ofNullable(result);
    }
}
