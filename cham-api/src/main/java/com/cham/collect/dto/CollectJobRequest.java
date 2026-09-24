package com.cham.collect.dto;

/**
 * '수집' 버튼 요청.
 *
 * @param sourceId 비우면 사용 중인 기관 전체
 * @param year     달을 고른 경우. year·month 는 같이 채우거나 같이 비운다
 * @param month
 */
public record CollectJobRequest(Long sourceId, Integer year, Integer month) {
}
