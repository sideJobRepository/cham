package com.cham.service.impl;

import com.cham.controller.response.ApiResponse;
import com.cham.entity.CardUse;
import com.cham.entity.CardUseAddr;
import com.cham.entity.dto.CardOwnerPositionDto;
import com.cham.entity.dto.CardUseAddrDto;
import com.cham.excel.PoiUtil;
import com.cham.repository.CardUseRepository;
import com.cham.service.CardUseAddrService;
import com.cham.service.CardUseService;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static com.cham.entity.QCardOwnerPosition.cardOwnerPosition;
import static com.cham.entity.QCardUseAddr.cardUseAddr;


@RequiredArgsConstructor
@Service
@Transactional
public class CardUseServiceImpl implements CardUseService {
    
    private final CardUseRepository cardUseRepository;
    
    private final CardUseAddrService cardUseAddrService;
    
    private final JPAQueryFactory queryFactory;
    
    
    @Override
    public ApiResponse insertCardUse(MultipartFile multipartFile) {
        List<CardOwnerPositionDto> cardOwnerPositionDtos = queryFactory
                .select(Projections.constructor(
                        CardOwnerPositionDto.class,
                        cardOwnerPosition.cardOwnerPositionId,
                        cardOwnerPosition.cardOwnerPositionName
                ))
                .from(cardOwnerPosition)
                .fetch();
        
        List<CardUseAddrDto> cardUseAddrDtos = queryFactory
                .select(Projections.constructor(
                        CardUseAddrDto.class,
                        cardUseAddr.cardUseAddrId,
                        cardUseAddr.cardUseAddrName,
                        cardUseAddr.cardUseDetailAddr
                ))
                .from(cardUseAddr)
                .fetch();
        
        try (InputStream is = multipartFile.getInputStream()) {
            Workbook workbook = WorkbookFactory.create(is);
            
            Sheet sheet = workbook.getSheetAt(0);
            for (Row row : sheet) {
                if (row.getRowNum() == 0) {
                    continue;
                }
                Cell userSell = row.getCell(0);
                String nameSell = PoiUtil.getCellValue(row, 1);
                Cell dateCell = row.getCell(2); // 집행일자
                Cell timeSell = row.getCell(3); // 시간
                Cell addrSell = row.getCell(4); // 사용장소
                String addrDetailValue = PoiUtil.getCellValue(row,5).trim(); // 상세주소
                Cell purposeSell = row.getCell(6); // 집행목적
                Cell personnelSell = row.getCell(7); //대상인원
                Cell amountCell = row.getCell(8); // 금액
                Cell methodCell = row.getCell(9); // 금액
                Cell remarkCell = row.getCell(10); // 비고
                
                String personnelStr = switch (personnelSell.getCellType()) {
                    case STRING -> personnelSell.getStringCellValue();
                    case NUMERIC -> String.valueOf((int) personnelSell.getNumericCellValue());
                    case FORMULA -> switch (personnelSell.getCachedFormulaResultType()) {
                        case STRING -> personnelSell.getStringCellValue();
                        case NUMERIC -> String.valueOf((int) personnelSell.getNumericCellValue());
                        default -> ""; // 혹시 모를 대비
                    };
                    default -> "";
                };
                
                CardUseAddr cardUserAddr = cardUseAddrDtos.stream()
                        .filter(dto -> addrDetailValue.equals(dto.getCardUseDetailAddr().trim()))
                        .findFirst()
                        .map(dto -> new CardUseAddr(dto.getCardUseAddrId()))
                        .orElseGet(() -> {
                            CardUseAddr inserted = cardUseAddrService.insertCardUseAddr(
                                    new CardUseAddr(addrSell.getStringCellValue(), addrDetailValue)
                            );
                            // 추가로 메모리에도 넣어줘야 이후 중복 insert 방지됨
                            cardUseAddrDtos.add(new CardUseAddrDto(
                                    inserted.getCardUseAddrId(),
                                    inserted.getCardUseAddrName(),
                                    inserted.getCardUseDetailAddr()
                            ));
                            return inserted;
                        });
                
                
                
                double excelDateValue = dateCell.getNumericCellValue();
                LocalDate dateValue = DateUtil.getLocalDateTime(excelDateValue).toLocalDate();
                LocalTime timeValue = PoiUtil.getLocalTimeFromCell(timeSell);
                
                CardUse cardUse = new CardUse(
                        cardOwnerPositionDtos,
                        cardUserAddr,
                        userSell.getStringCellValue(),
                        nameSell,
                        dateValue,
                        timeValue,
                        purposeSell.getStringCellValue(),
                        personnelStr,
                        amountCell.getNumericCellValue(),
                        methodCell.getStringCellValue(),
                        remarkCell.getStringCellValue()
                );
                cardUseRepository.save(cardUse);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return new ApiResponse(200 , true,"성공");
    }
}
