package com.cham.collect.dto;

import com.cham.collect.enumeration.CollectFileStatus;

import java.time.LocalDateTime;

/** 월 표를 만들 때 읽는 파일 한 줄 (저장소 → 서비스) */
public record CollectFileCell(Long fileId, Long sourceId, Integer month, CollectFileStatus status,
                              LocalDateTime collectedAt) {
}
