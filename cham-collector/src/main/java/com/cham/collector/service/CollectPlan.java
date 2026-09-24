package com.cham.collector.service;

import java.time.YearMonth;

/**
 * 한 번 수집할 때의 규칙.
 *
 * @param pageLimit             목록을 몇 페이지까지 볼지
 * @param minPeriod             이 달보다 앞선 게시물은 받지 않는다 (2026-08)
 * @param target                달을 골라 요청한 경우 그 달 게시물만 받는다. null 이면 minPeriod 이후 전부
 * @param skipIfLastMonthExists 정기 실행: 직전 달 자료가 이미 있는 기관은 목록도 열지 않는다
 * @param deepCheck             수동 실행: 이미 받은 게시물도 상세를 열어 새 첨부가 있는지 본다
 */
public record CollectPlan(int pageLimit, YearMonth minPeriod, YearMonth target,
                          boolean skipIfLastMonthExists, boolean deepCheck) {
}
