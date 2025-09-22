package com.cham.caruse.repository.impl;

import com.cham.caruse.entity.ChamMonimapCardUse;
import com.cham.caruse.repository.query.ChamMonimapCardUseQueryRepository;
import com.cham.dto.request.CardUseConditionRequest;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;

import java.util.List;

import static com.cham.carduseaddr.entity.QChamMonimapCardUseAddr.chamMonimapCardUseAddr;
import static com.cham.caruse.entity.QChamMonimapCardUse.chamMonimapCardUse;

@RequiredArgsConstructor
public class ChamMonimapCardUseRepositoryImpl implements ChamMonimapCardUseQueryRepository {
    
    private final JPAQueryFactory queryFactory;
    
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
        queryFactory
                .delete(chamMonimapCardUse)
                .where(chamMonimapCardUse.chamMonimapCardUseDelkey.eq(cardUseDelkey))
                .execute();
    }
    
    @Override
    public List<ChamMonimapCardUse> findByCardUses(CardUseConditionRequest request) {
        return queryFactory
                .selectFrom(chamMonimapCardUse)
                .join(chamMonimapCardUse.cardUseAddr, chamMonimapCardUseAddr).fetchJoin()
                .where(
                        chamMonimapCardUse.chamMonimapCardUseAmount.goe(100000),
                        chamMonimapCardUse.cardUseAddr.chamMonimapCardUseAddrName.contains("플라워")// 화환 제외
                                .or(chamMonimapCardUse.cardUseAddr.chamMonimapCardUseAddrName.contains("경조사비")) //경조사비 제외
                                .not(),
                        cardOwnerPositionEq(request),
                        cardUseNameLike(request),
                        cardUseDetailAddrLike(request),
                        cardUseAddrNameLike(request),
                        cardUseDate(request)
                )
                .fetch();
    }
    
    private BooleanExpression cardOwnerPositionEq(CardUseConditionRequest request) {
        if (request.getCardOwnerPositionId() != null) {
            return chamMonimapCardUse.chamMonimapCardOwnerPosition.chamMonimapCardOwnerPositionId.eq(request.getCardOwnerPositionId());
        }
        return null;
    }
    
    private BooleanExpression cardUseNameLike(CardUseConditionRequest request) {
        if (StringUtils.hasText(request.getCardUseName())) {
            return chamMonimapCardUse.chamMonimapCardUseName.like("%" + request.getCardUseName().trim() + "%");
        }
        return null;
    }
    
    private BooleanExpression cardUseDetailAddrLike(CardUseConditionRequest request) {
        if (StringUtils.hasText(request.getAddrDetail())) {
            return chamMonimapCardUse.cardUseAddr.chamMonimapCardUseDetailAddr.like("%" + request.getAddrDetail().trim() + "%");
        }
        return null;
    }
    private BooleanExpression cardUseAddrNameLike(CardUseConditionRequest request) {
        if (StringUtils.hasText(request.getAddrName())) {
           return  chamMonimapCardUse.cardUseAddr.chamMonimapCardUseAddrName.like("%" + request.getAddrName().trim() + "%");
        }
        return null;
    }
    private BooleanExpression cardUseDate(CardUseConditionRequest req) {
        if (req.getStartDate() != null && req.getEndDate() != null) {
            return chamMonimapCardUse.chamMonimapCardUseDate.between(req.getStartDate(), req.getEndDate());
        }
        if (req.getStartDate() != null) {
            return chamMonimapCardUse.chamMonimapCardUseDate.goe(req.getStartDate());
        }
        if (req.getEndDate() != null) {
            return chamMonimapCardUse.chamMonimapCardUseDate.loe(req.getEndDate());
        }
        return null;
    }
}
