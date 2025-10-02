package com.cham.caruse.dto;

import com.cham.dto.response.CardUseResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;
import java.util.Map;

@Getter
@AllArgsConstructor
public class CardUseAggregateResponse {
    private Map<Long, CardUseResponse> details;   // 기존 주소별 상세
    private List<RegionSummaryDto> summaries; // 추가된 요약
}
