package com.cham.collect.dto;

import com.cham.collect.parser.HeaderMappedSheetParser;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * 반영 전 미리보기.
 *
 * @param blockingRows 날짜를 못 읽은 줄 수. 0 이 아니면 반영할 수 없다
 * @param noNameRows   기존 자료에서 이름을 못 찾아 '공무원'(또는 화면에서 적은 이름)을 넣은 줄 수
 * @param rows         앞에서부터 limit 줄
 */
public record CollectPreviewResponse(
        Long fileId,
        String sourceName,
        Integer year,
        Integer month,
        String suggestedDeleteKey,
        String defaultName,
        boolean placeMissing,
        int totalRows,
        long warningRows,
        long blockingRows,
        long noNameRows,
        List<HeaderMappedSheetParser.SheetInfo> sheets,
        List<String> fileWarnings,
        List<Row> rows
) {

    public record Row(
            String sheet,
            int rowNum,
            String position,
            String region,
            String user,
            String name,
            LocalDate date,
            LocalTime time,
            String addrName,
            String addrDetail,
            String purpose,
            String personnel,
            Double amount,
            String method,
            String remark,
            List<String> warnings,
            boolean blocking
    ) {
    }
}
