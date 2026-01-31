package com.cham.feedbacck.legislationarticle.service.impl;

import com.cham.feedbacck.legislation.entity.Legislation;
import com.cham.feedbacck.legislation.repository.LegislationRepository;
import com.cham.feedbacck.legislationarticle.entity.LegislationArticle;
import com.cham.feedbacck.legislationarticle.repository.LegislationArticleRepository;
import com.cham.feedbacck.legislationarticle.service.LegislationArticleService;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;


@RequiredArgsConstructor
@Transactional
@Service
public class LegislationArticleServiceImpl implements LegislationArticleService {
    
    private final LegislationRepository legislationRepository;
    
    private final LegislationArticleRepository legislationArticleRepository;
    
    @Override
    public void insertExcel(MultipartFile excel) {

        try (InputStream is = excel.getInputStream();
             Workbook workbook = WorkbookFactory.create(is)) {

            Sheet sheet = workbook.getSheetAt(0);

            // 법안 먼저 생성 (bill_version 기준)
            // 지금은 엑셀 하나 = 법안 하나라고 가정
            Row firstDataRow = sheet.getRow(1);

            String billVersion = getString(firstDataRow.getCell(0));

            Legislation legislation = legislationRepository
                    .findByBillVersion(billVersion)
                    .orElseGet(() -> legislationRepository.save(
                            Legislation.builder()
                                    .title("대전충남통합특별법")
                                    .billVersion(billVersion)
                                    .build()
                    ));

            // 조문 insert
            int orderNo = 1;

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                String part = getString(row.getCell(1));
                String chapter = getString(row.getCell(2));
                String section = getString(row.getCell(3));
                String articleNo = getString(row.getCell(4));
                String articleTitle = getString(row.getCell(5));
                String content = getString(row.getCell(6));
                String categoryMain = getString(row.getCell(7));
                String categorySub = getString(row.getCell(8));

                if (articleNo == null || articleNo.isBlank()) continue;

                LegislationArticle article = LegislationArticle.builder()
                        .legislation(legislation)
                        .part(part)
                        .chapter(chapter)
                        .section(section) // null 허용
                        .articleNo(articleNo)
                        .articleTitle(articleTitle)
                        .cont(content)
                        .categoryMain(categoryMain)
                        .categorySub(categorySub)
                        .ordersNo(orderNo++)
                        .build();

                legislationArticleRepository.save(article);
            }

        } catch (Exception e) {
            throw new RuntimeException("엑셀 업로드 실패", e);
        }
    }

    /**
     * 셀 안전하게 문자열로 변환
     */
    private String getString(Cell cell) {
        if (cell == null) return null;

        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> {
                if (DateUtil.isCellDateFormatted(cell)) {
                    yield cell.getLocalDateTimeCellValue().toString();
                }
                yield String.valueOf((long) cell.getNumericCellValue());
            }
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> cell.getCellFormula();
            default -> null;
        };
    }
}
