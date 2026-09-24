package com.cham.caruse.repository.impl;

import com.cham.cardowner.entity.QChamMonimapCardOwnerPosition;
import com.cham.caruse.CardUseDefaults;
import com.cham.caruse.CleanupRules;
import com.cham.caruse.dto.CardUseUploadMemberResponse;
import com.cham.caruse.dto.CardUseUploadResponse;
import com.cham.caruse.dto.CleanupAddrResponse;
import com.cham.caruse.dto.CleanupNameResponse;
import com.cham.caruse.entity.ChamMonimapCardUse;
import com.cham.caruse.repository.dto.CardUseSummaryDto;
import com.cham.caruse.repository.query.ChamMonimapCardUseQueryRepository;
import com.cham.dto.request.CardUseConditionRequest;
import com.cham.region.entity.QChamMonimapRegion;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.util.StringUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.cham.cardowner.entity.QChamMonimapCardOwnerPosition.*;
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
                .join(chamMonimapCardUse.chamMonimapCardOwnerPosition,chamMonimapCardOwnerPosition).fetchJoin()
                .join(dong.parent, gu).fetchJoin()
                .join(gu.parent, city).fetchJoin()
                .where(
                        chamMonimapCardUseAddr.chamMonimapCardUseAddrName.notLike("%플라워%"),
                        chamMonimapCardUseAddr.chamMonimapCardUseAddrName.notLike("%경조사비%"),
                        chamMonimapCardUseAddr.chamMonimapCardUseAddrName.notLike("%직원%"),
                        isPublic(),
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
                .join(chamMonimapCardUse.chamMonimapCardOwnerPosition,chamMonimapCardOwnerPosition).fetchJoin()
                .join(dong.parent, gu).fetchJoin()
                .join(gu.parent, city).fetchJoin()
                .where(
                        chamMonimapCardUse.cardUseAddr.chamMonimapCardUseAddrName.contains("플라워")// 화환 제외
                                .or(chamMonimapCardUse.cardUseAddr.chamMonimapCardUseAddrName.contains("경조사비")) //경조사비 제외
                                .or(chamMonimapCardUse.cardUseAddr.chamMonimapCardUseAddrName.contains("직원")) //직원 제외
                                .not(),
                        isPublic(),
                        cardUseDetailAddrLike(cardUsesDetail)
                )
                .fetch();
    }
    
    @Override
    public List<CardUseSummaryDto> findBySumTotalAmount() {
        
         return queryFactory
                .select(Projections.fields(
                        CardUseSummaryDto.class,
                        chamMonimapCardUseAddr.chamMonimapCardUseAddrId,
                        chamMonimapCardUseAddr.chamMonimapCardUseAddrName,
                        numberTemplate(Integer.class, "sum({0})", chamMonimapCardUse.chamMonimapCardUseAmount).as("totalAmount")
                ))
                .from(chamMonimapCardUse)
                .join(chamMonimapCardUse.cardUseAddr, chamMonimapCardUseAddr)
                .where(
                        chamMonimapCardUse.cardUseAddr.chamMonimapCardUseAddrName.contains("플라워")// 화환 제외
                                .or(chamMonimapCardUse.cardUseAddr.chamMonimapCardUseAddrName.contains("경조사비")) //경조사비 제외
                                .or(chamMonimapCardUse.cardUseAddr.chamMonimapCardUseAddrName.contains("직원")) //직원 제외
                                .not(),
                        isPublic()
                )
                .groupBy(
                        chamMonimapCardUseAddr.chamMonimapCardUseAddrId,
                        chamMonimapCardUseAddr.chamMonimapCardUseAddrName
                        )
                .fetch();
    }
    
    @Override
    public List<CardUseUploadResponse> findCardUseUploads() {
        return queryFactory
                .select(Projections.constructor(
                        CardUseUploadResponse.class,
                        chamMonimapCardUse.chamMonimapCardUseDelkey,
                        chamMonimapCardOwnerPosition.chamMonimapCardOwnerPositionName.min(),
                        chamMonimapCardUse.count(),
                        chamMonimapCardUse.chamMonimapCardUseDate.min(),
                        chamMonimapCardUse.chamMonimapCardUseDate.max(),
                        chamMonimapCardUse.registDate.min(),
                        chamMonimapCardUse.chamMonimapCardUsePublic
                ))
                .from(chamMonimapCardUse)
                .leftJoin(chamMonimapCardUse.chamMonimapCardOwnerPosition, chamMonimapCardOwnerPosition)
                .where(chamMonimapCardUse.chamMonimapCardUseDelkey.isNotNull())
                // 공개 여부까지 묶는다. 전환이 언제나 삭제키 단위 벌크 UPDATE 라 한 삭제키 안에서
                // 값이 갈릴 일이 없고, 그래야 집계 함수 없이 컬럼을 그대로 내릴 수 있다.
                .groupBy(
                        chamMonimapCardUse.chamMonimapCardUseDelkey,
                        chamMonimapCardUse.chamMonimapCardUsePublic
                )
                .orderBy(chamMonimapCardUse.registDate.min().desc())
                .fetch();
    }

    @Override
    public List<CardUseUploadMemberResponse> findCardUseUploadMembers() {
        return queryFactory
                .select(Projections.constructor(
                        CardUseUploadMemberResponse.class,
                        chamMonimapCardUse.chamMonimapCardUseDelkey,
                        chamMonimapCardUse.chamMonimapCardUseName,
                        chamMonimapCardUse.count()
                ))
                .from(chamMonimapCardUse)
                .where(
                        chamMonimapCardUse.chamMonimapCardUseDelkey.isNotNull(),
                        chamMonimapCardUse.chamMonimapCardUseName.isNotNull(),
                        chamMonimapCardUse.chamMonimapCardUseName.ne("")
                )
                .groupBy(
                        chamMonimapCardUse.chamMonimapCardUseDelkey,
                        chamMonimapCardUse.chamMonimapCardUseName
                )
                // 건수가 많은 사람이 위로. 명단을 줄여 보여줄 때 대표자가 먼저 오게 된다.
                .orderBy(chamMonimapCardUse.count().desc(),
                         chamMonimapCardUse.chamMonimapCardUseName.asc())
                .fetch();
    }

    @Override
    public long updatePublicByDelkey(String cardUseDelkey, boolean isPublic) {
        // 벌크 DML 은 영속성 컨텍스트를 건너뛴다. 삭제와 같은 방식으로 앞뒤를 맞춰준다.
        em.flush();
        long affected = queryFactory
                .update(chamMonimapCardUse)
                .set(chamMonimapCardUse.chamMonimapCardUsePublic, isPublic)
                .where(chamMonimapCardUse.chamMonimapCardUseDelkey.eq(cardUseDelkey))
                .execute();
        em.clear();
        return affected;
    }

    @Override
    public long updateDelkey(String cardUseDelkey, String newCardUseDelkey) {
        em.flush();
        long affected = queryFactory
                .update(chamMonimapCardUse)
                .set(chamMonimapCardUse.chamMonimapCardUseDelkey, newCardUseDelkey)
                .where(chamMonimapCardUse.chamMonimapCardUseDelkey.eq(cardUseDelkey))
                .execute();
        em.clear();
        return affected;
    }

    @Override
    public List<ChamMonimapCardUse> findByDelkeyForExport(String cardUseDelkey) {
        // 엑셀에는 비공개 자료도 그대로 담는다. 관리자가 원본을 받아보는 용도다.
        return queryFactory
                .selectFrom(chamMonimapCardUse)
                .leftJoin(chamMonimapCardUse.cardUseAddr, chamMonimapCardUseAddr).fetchJoin()
                .leftJoin(chamMonimapCardUse.chamMonimapCardOwnerPosition, chamMonimapCardOwnerPosition).fetchJoin()
                .where(chamMonimapCardUse.chamMonimapCardUseDelkey.eq(cardUseDelkey))
                .orderBy(
                        chamMonimapCardUse.chamMonimapCardUseDate.asc(),
                        chamMonimapCardUse.chamMonimapCardUseTime.asc(),
                        chamMonimapCardUse.chamMonimapCardUseId.asc()
                )
                .fetch();
    }

    @Override
    public Page<CleanupAddrResponse> findCleanupAddrs(String keyword, boolean onlyIssues, Pageable pageable) {
        // 기본은 전부 보여준다. 손봐야 할 것을 가려내는 조건이 단어 매칭이라
        // '어딘지 모르겠어요' 는 잡아도 'ㅁㄴㅇㄹ' 같은 것은 놓친다.
        // 걸러서 숨겨 버리면 놓친 줄은 영영 눈에 안 띈다. 숨기는 대신 맨 위로 올린다.
        BooleanExpression condition = and(
                StringUtils.hasText(keyword) ? addrKeyword(keyword) : null,
                onlyIssues ? suspiciousAddr() : null);

        List<CleanupAddrResponse> content = queryFactory
                .select(Projections.constructor(
                        CleanupAddrResponse.class,
                        chamMonimapCardUseAddr.chamMonimapCardUseAddrId,
                        chamMonimapCardUseAddr.chamMonimapCardUseAddrName,
                        chamMonimapCardUseAddr.chamMonimapCardUseDetailAddr,
                        chamMonimapCardUseAddr.chamMonimapCardUseXValue,
                        chamMonimapCardUseAddr.chamMonimapCardUseYValue,
                        chamMonimapCardUse.count()
                ))
                .from(chamMonimapCardUse)
                .join(chamMonimapCardUse.cardUseAddr, chamMonimapCardUseAddr)
                .where(condition)
                .groupBy(
                        chamMonimapCardUseAddr.chamMonimapCardUseAddrId,
                        chamMonimapCardUseAddr.chamMonimapCardUseAddrName,
                        chamMonimapCardUseAddr.chamMonimapCardUseDetailAddr,
                        chamMonimapCardUseAddr.chamMonimapCardUseXValue,
                        chamMonimapCardUseAddr.chamMonimapCardUseYValue
                )
                // 손봐야 할 것을 맨 위로, 그다음 건수 많은 순.
                // 같은 건수면 ID 로 순서를 고정해야 페이지를 넘길 때 같은 줄이
                // 두 번 나오거나 건너뛰는 일이 없다.
                .orderBy(addrIssueOrder().asc(),
                         chamMonimapCardUse.count().desc(),
                         chamMonimapCardUseAddr.chamMonimapCardUseAddrId.asc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // group by 결과의 전체 개수다. 묶인 줄이 몇 개인지를 세야 해서 count 를 한 번 더 감싼다.
        JPAQuery<Long> countQuery = queryFactory
                .select(chamMonimapCardUseAddr.chamMonimapCardUseAddrId.countDistinct())
                .from(chamMonimapCardUse)
                .join(chamMonimapCardUse.cardUseAddr, chamMonimapCardUseAddr)
                .where(condition);

        return PageableExecutionUtils.getPage(content, pageable, () -> {
            Long total = countQuery.fetchOne();
            return total == null ? 0L : total;
        });
    }

    // 손봐야 할 장소를 가려내는 기준.
    // 좌표가 없거나(핀을 못 찍는다), 상세주소가 자리값 그대로거나(엉뚱한 데 찍힌다),
    // 장소명에 사람이 적어 넣은 메모가 남아 있는 경우다.
    private BooleanExpression suspiciousAddr() {
        return chamMonimapCardUseAddr.chamMonimapCardUseXValue.isNull()
                .or(chamMonimapCardUseAddr.chamMonimapCardUseXValue.eq(""))
                .or(chamMonimapCardUseAddr.chamMonimapCardUseYValue.isNull())
                .or(chamMonimapCardUseAddr.chamMonimapCardUseYValue.eq(""))
                .or(chamMonimapCardUseAddr.chamMonimapCardUseDetailAddr.eq(CardUseDefaults.DETAIL_ADDR))
                .or(containsAny(chamMonimapCardUseAddr.chamMonimapCardUseAddrName,
                        CleanupRules.ADDR_NAME_WORDS));
    }

    // 단어 목록은 CleanupRules 한곳에서 가져온다. 화면 배지도 같은 목록을 본다.
    private BooleanExpression containsAny(StringPath path, List<String> words) {
        return words.stream()
                .map(path::contains)
                .reduce(BooleanExpression::or)
                .orElseThrow();
    }

    // null 은 조건 없음으로 본다. 둘 다 null 이면 전체를 뜻하는 null 을 그대로 돌려준다.
    private BooleanExpression and(BooleanExpression left, BooleanExpression right) {
        if (left == null) return right;
        if (right == null) return left;
        return left.and(right);
    }

    // 손봐야 할 줄은 0, 멀쩡한 줄은 1. 오름차순으로 정렬하면 문제되는 것이 위로 올라온다.
    private NumberExpression<Integer> addrIssueOrder() {
        return new CaseBuilder().when(suspiciousAddr()).then(0).otherwise(1);
    }

    private NumberExpression<Integer> nameIssueOrder() {
        return new CaseBuilder().when(suspiciousName()).then(0).otherwise(1);
    }

    private BooleanExpression addrKeyword(String keyword) {
        return chamMonimapCardUseAddr.chamMonimapCardUseAddrName.contains(keyword)
                .or(chamMonimapCardUseAddr.chamMonimapCardUseDetailAddr.contains(keyword));
    }

    @Override
    public Page<CleanupNameResponse> findCleanupNames(String keyword, boolean onlyIssues, Pageable pageable) {
        BooleanExpression condition = and(
                StringUtils.hasText(keyword)
                        ? chamMonimapCardUse.chamMonimapCardUseName.contains(keyword)
                        : null,
                onlyIssues ? suspiciousName() : null);

        BooleanExpression notEmpty = chamMonimapCardUse.chamMonimapCardUseName.isNotNull()
                .and(chamMonimapCardUse.chamMonimapCardUseName.ne(""));

        List<CleanupNameResponse> content = queryFactory
                .select(Projections.constructor(
                        CleanupNameResponse.class,
                        chamMonimapCardUse.chamMonimapCardUseName,
                        chamMonimapCardUse.count()
                ))
                .from(chamMonimapCardUse)
                .where(notEmpty, condition)
                .groupBy(chamMonimapCardUse.chamMonimapCardUseName)
                // 손봐야 할 것을 맨 위로, 그다음 건수 많은 순.
                // 건수가 같을 때는 이름으로 순서를 고정한다. 페이지를 넘길 때 순서가
                // 흔들리면 같은 줄이 두 번 나오거나 빠진다.
                .orderBy(nameIssueOrder().asc(),
                         chamMonimapCardUse.count().desc(),
                         chamMonimapCardUse.chamMonimapCardUseName.asc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(chamMonimapCardUse.chamMonimapCardUseName.countDistinct())
                .from(chamMonimapCardUse)
                .where(notEmpty, condition);

        return PageableExecutionUtils.getPage(content, pageable, () -> {
            Long total = countQuery.fetchOne();
            return total == null ? 0L : total;
        });
    }

    // 괄호나 물음표가 붙은 표기는 사람이 메모를 남긴 것이다. 같은 사람이 둘로 갈리는 원인이 된다.
    private BooleanExpression suspiciousName() {
        return containsAny(chamMonimapCardUse.chamMonimapCardUseName,
                CleanupRules.MEMBER_NAME_WORDS);
    }

    @Override
    public long updateCardUseName(String oldName, String newName) {
        em.flush();
        long affected = queryFactory
                .update(chamMonimapCardUse)
                .set(chamMonimapCardUse.chamMonimapCardUseName, newName)
                .where(chamMonimapCardUse.chamMonimapCardUseName.eq(oldName))
                .execute();
        em.clear();
        return affected;
    }

    // 지도에 나가는 조회는 전부 공개된 것만 본다.
    private BooleanExpression isPublic() {
        return chamMonimapCardUse.chamMonimapCardUsePublic.isTrue();
    }

    private BooleanExpression cardOwnerPositionEq(CardUseConditionRequest request) {
        if (request.getCardOwnerPositionId() != null) {
            return chamMonimapCardUse.chamMonimapCardOwnerPosition.chamMonimapCardOwnerPositionId.eq(request.getCardOwnerPositionId());
        }
        return null;
    }
    private BooleanExpression inputOrCondition(CardUseConditionRequest request) {
        String input = request.getInput();
        // 지역 /사용자 / 이름 / 집행목적 // 가게이름
        if (StringUtils.hasText(input)) {
            return chamMonimapCardUse.chamMonimapCardUseRegion.like("%" + input + "%")
                    .or(chamMonimapCardUse.chamMonimapCardUseUser.like("%" + input + "%"))
                    .or(chamMonimapCardUse.chamMonimapCardUseName.like("%" + input + "%"))
                    .or(chamMonimapCardUse.chamMonimapCardUsePurpose.like("%" + input + "%"))
                    .or(chamMonimapCardUse.cardUseAddr.chamMonimapCardUseAddrName.like("%" + input + "%"));
        }
        return null;
    }
    
    @Override
    public Map<String, String> findLatestNameByUser(String region) {
        // 최근 행부터 읽어 사용자마다 처음 나온 이름만 남긴다. 한 지역 몇백~몇천 줄이라 그대로 읽어도 된다
        List<Tuple> rows = queryFactory
                .select(chamMonimapCardUse.chamMonimapCardUseUser, chamMonimapCardUse.chamMonimapCardUseName)
                .from(chamMonimapCardUse)
                .where(chamMonimapCardUse.chamMonimapCardUseRegion.eq(region),
                        chamMonimapCardUse.chamMonimapCardUseUser.isNotNull(),
                        chamMonimapCardUse.chamMonimapCardUseName.isNotNull())
                .orderBy(chamMonimapCardUse.chamMonimapCardUseDate.desc(),
                        chamMonimapCardUse.chamMonimapCardUseId.desc())
                .fetch();

        Map<String, String> result = new LinkedHashMap<>();
        for (Tuple row : rows) {
            String user = row.get(chamMonimapCardUse.chamMonimapCardUseUser).trim();
            String name = row.get(chamMonimapCardUse.chamMonimapCardUseName).trim();
            if (!user.isEmpty() && !name.isEmpty()) {
                result.putIfAbsent(user, name);
            }
        }
        return result;
    }

    private BooleanExpression cardUseDetailAddrLike(String cardUsesDetail) {
        if (StringUtils.hasText(cardUsesDetail)) {
            return chamMonimapCardUse.cardUseAddr.chamMonimapCardUseDetailAddr.like("%" + cardUsesDetail + "%");
        }
        return null;
    }
}
