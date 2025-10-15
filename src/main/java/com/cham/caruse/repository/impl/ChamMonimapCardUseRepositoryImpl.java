package com.cham.caruse.repository.impl;

import com.cham.caruse.entity.ChamMonimapCardUse;
import com.cham.caruse.repository.dto.CardUseSummaryDto;
import com.cham.caruse.repository.query.ChamMonimapCardUseQueryRepository;
import com.cham.dto.request.CardUseConditionRequest;
import com.cham.region.entity.QChamMonimapRegion;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;

import java.util.List;

import static com.cham.carduseaddr.entity.QChamMonimapCardUseAddr.chamMonimapCardUseAddr;
import static com.cham.caruse.entity.QChamMonimapCardUse.chamMonimapCardUse;
import static com.querydsl.core.types.dsl.Expressions.numberTemplate;
@RequiredArgsConstructor
public class ChamMonimapCardUseRepositoryImpl implements ChamMonimapCardUseQueryRepository {
    
    private final JPAQueryFactory queryFactory;
    
    private final EntityManager em;
    
    @Override
    public boolean existsByChamMonimapCardUseDelkey(String cardUseDelkey) {
        Integer fetchOne = queryFactory
                .selectOne()
                .from(chamMonimapCardUse)
                .where(chamMonimapCardUse.chamMonimapCardUseDelkey.eq(cardUseDelkey))
                .fetchFirst();
        
        return fetchOne != null;
    }
    
    @Override
    public void deleteByCardUseDelkey(String cardUseDelkey) {
        em.flush();
        queryFactory
                .delete(chamMonimapCardUse)
                .where(chamMonimapCardUse.chamMonimapCardUseDelkey.eq(cardUseDelkey))
                .execute();
        em.clear();
    }
    
    @Override
    public List<ChamMonimapCardUse> findByCardUses(CardUseConditionRequest request) {
        QChamMonimapRegion dong = new QChamMonimapRegion("dong");
        QChamMonimapRegion gu   = new QChamMonimapRegion("gu");
        QChamMonimapRegion city = new QChamMonimapRegion("city");
        
        return queryFactory
                .selectFrom(chamMonimapCardUse)
                .join(chamMonimapCardUse.cardUseAddr, chamMonimapCardUseAddr).fetchJoin()
                .join(chamMonimapCardUseAddr.chamMonimapRegion, dong).fetchJoin()
                .join(dong.parent, gu).fetchJoin()
                .join(gu.parent, city).fetchJoin()
                .where(
                        chamMonimapCardUse.chamMonimapCardUseAmount.goe(100000),
                        chamMonimapCardUseAddr.chamMonimapCardUseAddrName.notLike("%플라워%"),
                        chamMonimapCardUseAddr.chamMonimapCardUseAddrName.notLike("%경조사비%"),
                        chamMonimapCardUseAddr.chamMonimapCardUseAddrName.notLike("%직원%"),
                        cardOwnerPositionEq(request),
                        inputOrCondition(request)
                )
                .fetch();
    }
    
    @Override
    public List<ChamMonimapCardUse> findByCardUsesDetail(String cardUsesDetail) {
        QChamMonimapRegion dong = new QChamMonimapRegion("dong");
        QChamMonimapRegion gu   = new QChamMonimapRegion("gu");
        QChamMonimapRegion city = new QChamMonimapRegion("city");
        return queryFactory
                .selectFrom(chamMonimapCardUse)
                .join(chamMonimapCardUse.cardUseAddr, chamMonimapCardUseAddr).fetchJoin()
                .join(chamMonimapCardUseAddr.chamMonimapRegion, dong).fetchJoin()
                .join(dong.parent, gu).fetchJoin()
                .join(gu.parent, city).fetchJoin()
                .where(
                        chamMonimapCardUse.chamMonimapCardUseAmount.goe(100000),
                        chamMonimapCardUse.cardUseAddr.chamMonimapCardUseAddrName.contains("플라워")// 화환 제외
                                .or(chamMonimapCardUse.cardUseAddr.chamMonimapCardUseAddrName.contains("경조사비")) //경조사비 제외
                                .or(chamMonimapCardUse.cardUseAddr.chamMonimapCardUseAddrName.contains("직원")) //직원 제외
                                .not(),
                        cardUseDetailAddrLike(cardUsesDetail)
                )
                .fetch();
    }
    
    @Override
    public List<CardUseSummaryDto> findBySumTotalAmount() {
        
         return queryFactory
                .select(Projections.fields(
                        CardUseSummaryDto.class,
                        chamMonimapCardUseAddr.chamMonimapCardUseAddrName.as("name"),
                        numberTemplate(Integer.class, "sum({0})", chamMonimapCardUse.chamMonimapCardUseAmount).as("totalAmount")
                ))
                .from(chamMonimapCardUse)
                .join(chamMonimapCardUse.cardUseAddr, chamMonimapCardUseAddr)
                .where(
                        chamMonimapCardUse.chamMonimapCardUseAmount.goe(100000),
                        chamMonimapCardUse.cardUseAddr.chamMonimapCardUseAddrName.contains("플라워")// 화환 제외
                                .or(chamMonimapCardUse.cardUseAddr.chamMonimapCardUseAddrName.contains("경조사비")) //경조사비 제외
                                .or(chamMonimapCardUse.cardUseAddr.chamMonimapCardUseAddrName.contains("직원")) //직원 제외
                                .not()
                )
                .groupBy(
                        chamMonimapCardUseAddr.chamMonimapCardUseAddrName
                        )
                .fetch();
    }
    
    private BooleanExpression cardOwnerPositionEq(CardUseConditionRequest request) {
        if (request.getCardOwnerPositionId() != null) {
            return chamMonimapCardUse.chamMonimapCardOwnerPosition.chamMonimapCardOwnerPositionId.eq(request.getCardOwnerPositionId());
        }
        return null;
    }
    private BooleanExpression inputOrCondition(CardUseConditionRequest request) {
        String input = request.getInput();
        // 지역 /사용자 / 이름 / 집행목적
        if (StringUtils.hasText(input)) {
            return chamMonimapCardUse.chamMonimapCardUseRegion.like("%" + input + "%")
                    .or(chamMonimapCardUse.chamMonimapCardUseUser.like("%" + input + "%"))
                    .or(chamMonimapCardUse.chamMonimapCardUseName.like("%" + input + "%"))
                    .or(chamMonimapCardUse.chamMonimapCardUsePurpose.like("%" + input + "%"));
        }
        return null;
    }
    
    private BooleanExpression cardUseDetailAddrLike(String cardUsesDetail) {
        if (StringUtils.hasText(cardUsesDetail)) {
            return chamMonimapCardUse.cardUseAddr.chamMonimapCardUseDetailAddr.like("%" + cardUsesDetail + "%");
        }
        return null;
    }
}
