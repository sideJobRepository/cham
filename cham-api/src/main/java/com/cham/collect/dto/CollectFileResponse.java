package com.cham.collect.dto;

import com.cham.collect.enumeration.CollectFileStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record CollectFileResponse(
        Long fileId,
        Long sourceId,
        String sourceName,
        Integer year,
        Integer month,
        String postTitle,
        LocalDate postDate,
        String detailUrl,
        String originName,
        String ext,
        Long size,
        LocalDateTime collectedAt,
        CollectFileStatus status,
        String deleteKey,
        LocalDateTime importAt,
        String error
) {
}
