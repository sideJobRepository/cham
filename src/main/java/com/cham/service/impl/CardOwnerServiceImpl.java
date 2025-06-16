package com.cham.service.impl;


import com.cham.controller.response.ApiResponse;
import com.cham.excel.PoiUtil;
import com.cham.repository.CardOwnerRepository;
import com.cham.service.CardOwnerService;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;

@RequiredArgsConstructor
@Service
@Transactional
public class CardOwnerServiceImpl implements CardOwnerService {
    
    private final CardOwnerRepository cardOwnerRepository;
    private InputStream is;
    
    @Override
    public ApiResponse insertCardOwner(MultipartFile multipartFile) {
        
        
        try(InputStream is = multipartFile.getInputStream()) {
            Workbook workbook = WorkbookFactory.create(is);
            
            Sheet sheet = workbook.getSheetAt(0);
            for (Row row : sheet) {
                if(row.getRowNum() == 0){
                    continue;
                }
                Cell userSell = row.getCell(0);
                String nameSell = PoiUtil.getCellValue(row,1);
                Cell dateCell = row.getCell(2);
                
                double excelDateValue = dateCell.getNumericCellValue();
                LocalDateTime dateTime = DateUtil.getLocalDateTime(excelDateValue);
                LocalDate date = dateTime.toLocalDate();
            }
            
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        
        return null;
    }
}
