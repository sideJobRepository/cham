package com.cham.collect.dto;

import java.time.LocalDateTime;

public record CollectSourceResponse(
        Long sourceId,
        String code,
        String name,
        String engine,
        String listUrl,
        String positionName,
        String region,
        String defaultName,
        String fileFormat,
        Boolean enabled,
        LocalDateTime lastSuccess,
        String lastError,
        String note
) {
}
