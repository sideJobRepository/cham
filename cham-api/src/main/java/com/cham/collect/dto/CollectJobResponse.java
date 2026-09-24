package com.cham.collect.dto;

import com.cham.collect.enumeration.CollectJobStatus;
import com.cham.collect.enumeration.CollectJobTrigger;

import java.time.LocalDateTime;

public record CollectJobResponse(
        Long jobId,
        CollectJobTrigger trigger,
        Long requestedBy,
        String requestedByName,
        Long sourceId,
        String sourceName,
        Integer targetYear,
        Integer targetMonth,
        CollectJobStatus status,
        LocalDateTime startedAt,
        LocalDateTime finishedAt,
        Integer postsSeen,
        Integer filesNew,
        Integer filesSkipped,
        Integer filesFailed,
        String log,
        LocalDateTime registDate
) {
}
