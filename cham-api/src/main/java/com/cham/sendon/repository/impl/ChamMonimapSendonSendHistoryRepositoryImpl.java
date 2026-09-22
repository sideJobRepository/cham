package com.cham.sendon.repository.impl;

import com.cham.sendon.repository.query.ChamMonimapSendonSendHistoryQueryRepository;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class ChamMonimapSendonSendHistoryRepositoryImpl implements ChamMonimapSendonSendHistoryQueryRepository {

    private final JPAQueryFactory queryFactory;
    
}
