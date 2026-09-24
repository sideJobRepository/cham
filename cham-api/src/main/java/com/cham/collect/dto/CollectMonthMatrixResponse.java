package com.cham.collect.dto;

import com.cham.collect.enumeration.CollectFileStatus;

import java.util.List;

/**
 * 수집관리 월 표. 줄 = 기관, 칸 = 1~12월.
 *
 * @param startYear  자동수집 시작 연·월. 그 전 칸은 화면에서 흐리게 둔다
 * @param startMonth
 */
public record CollectMonthMatrixResponse(int year, int startYear, int startMonth, List<Row> rows) {

    public record Row(Long sourceId, String name, Boolean enabled, String fileFormat, String lastError,
                      List<Cell> months) {
    }

    /**
     * @param fileId    대표 파일(가장 최근에 받은 것). 없으면 null
     * @param status    대표 파일 상태. 없으면 null
     * @param fileCount 그 달에 받은 파일 수 (중복 제외)
     */
    public record Cell(int month, Long fileId, CollectFileStatus status, int fileCount) {
    }
}
