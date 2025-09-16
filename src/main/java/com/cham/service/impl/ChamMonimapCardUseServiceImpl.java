package com.cham.service.impl;

import com.cham.advice.exception.CustomException;
import com.cham.controller.request.CardUseConditionRequest;
import com.cham.controller.response.ApiResponse;
import com.cham.controller.response.CardUseGroupedResponse;
import com.cham.controller.response.CardUseResponse;
import com.cham.controller.response.ReplyResponse;
import com.cham.entity.*;
import com.cham.entity.dto.CardOwnerPositionDto;
import com.cham.entity.dto.CardUseAddrDto;
import com.cham.excel.PoiUtil;
import com.cham.repository.ChamMonimapCardOwnerPositionRepository;
import com.cham.repository.ChamMonimapCardUseAddrRepository;
import com.cham.repository.ChamMonimapCardUseRepository;
import com.cham.service.ChamMonimapCardUseAddrService;
import com.cham.service.ChamMonimapCardUseService;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.cham.entity.QChamMonimapCardOwnerPosition.*;
import static com.cham.entity.QChamMonimapCardUse.*;

import static com.cham.entity.QChamMonimapCardUseAddr.*;
import static com.cham.entity.QChamMonimapReply.*;
import static com.cham.entity.QChamMonimapReplyImage.*;


@RequiredArgsConstructor
@Service
@Transactional
public class ChamMonimapCardUseServiceImpl implements ChamMonimapCardUseService {
    
    private final ChamMonimapCardUseRepository cardUseRepository;
    
    private final ChamMonimapCardUseAddrRepository cardUseAddrRepository;
    
    private final ChamMonimapCardUseAddrService cardUseAddrService;
    
    private final ChamMonimapCardOwnerPositionRepository cardOwnerPositionRepository;
    
    private final JPAQueryFactory queryFactory;
    
    
    @Override
    public Map<Long, CardUseResponse> selectCardUse(CardUseConditionRequest request) {
        QChamMonimapCardUse cardUse = chamMonimapCardUse;
        QChamMonimapReply reply = chamMonimapReply;
        QChamMonimapReplyImage replyImage = chamMonimapReplyImage;
        QChamMonimapCardUseAddr cardUseAddr = chamMonimapCardUseAddr;
        BooleanBuilder booleanBuilder = new BooleanBuilder();

        if (request.getCardOwnerPositionId() != null) {
            booleanBuilder.and(cardUse.chamMonimapCardOwnerPosition.chamMonimapCardOwnerPositionId.eq(request.getCardOwnerPositionId()));
        }
        if (StringUtils.hasText(request.getCardUseName())) {
            booleanBuilder.and(cardUse.chamMonimapCardUseName.like("%" + request.getCardUseName().trim() + "%"));
        }
        
        if (StringUtils.hasText(request.getAddrDetail())) {
            booleanBuilder.and(cardUse.cardUseAddr.chamMonimapCardUseDetailAddr.like("%" + request.getAddrDetail().trim() + "%"));
        }
        
        if(StringUtils.hasText(request.getAddrName())) {
            booleanBuilder.and(cardUse.cardUseAddr.chamMonimapCardUseAddrName.like("%"+ request.getAddrName().trim() +"%"));
        }
        
        // 날짜 필터 조건 처리
        if (request.getStartDate() != null && request.getEndDate() != null) {
            booleanBuilder.and(cardUse.chamMonimapCardUseDate.between(request.getStartDate(), request.getEndDate()));
        } else if (request.getStartDate() != null) {
            booleanBuilder.and(cardUse.chamMonimapCardUseDate.goe(request.getStartDate()));
        } else if (request.getEndDate() != null) {
            booleanBuilder.and(cardUse.chamMonimapCardUseDate.loe(request.getEndDate()));
        }
        
        // 1. 카드사용 + 카드사용장소 + 댓글 조회
        List<ChamMonimapCardUse> cardUses = queryFactory
                .selectFrom(cardUse)
                .join(cardUse.cardUseAddr, cardUseAddr).fetchJoin()
                .where(booleanBuilder)
                .fetch();
        
        List<ChamMonimapReply> replies = queryFactory
                .selectFrom(reply)
                .join(reply.chamMonimapCardUseAddr, cardUseAddr).fetchJoin()
                .fetch();
        
        List<ChamMonimapReplyImage> replyImages = queryFactory
                .selectFrom(replyImage)
                .join(replyImage.chamMonimapReply, reply).fetchJoin()
                .fetch();
        
        // 2. 그룹핑: 장소별 카드사용, 장소별 댓글
        Map<Long, List<ChamMonimapCardUse>> groupedByAddrId = cardUses.stream()
                .collect(Collectors.groupingBy(use -> use.getCardUseAddr().getChamMonimapCardUseAddrId()));
        
        Map<Long, List<ChamMonimapReply>> repliesGroupedByAddrId = replies.stream()
                .collect(Collectors.groupingBy(r -> r.getChamMonimapCardUseAddr().getChamMonimapCardUseAddrId()));
        
        Map<Long, List<ChamMonimapReplyImage>> replyImageGroup = replyImages.stream()
                .collect(Collectors.groupingBy(img -> img.getChamMonimapReply().getChamMonimapReplyId()));
        
        Map<Long, CardUseResponse> resultMap = new LinkedHashMap<>();
        
        for (Map.Entry<Long, List<ChamMonimapCardUse>> entry : groupedByAddrId.entrySet()) {
            Long addrId = entry.getKey();
            List<ChamMonimapCardUse> cardUseList = entry.getValue();
            
            // 방문 횟수 조건 필터링
            if (request.getNumberOfVisits() != null && cardUseList.size() < request.getNumberOfVisits()) {
                continue;
            }
            
            ChamMonimapCardUse first = cardUseList.get(0);
            String addrName = first.getCardUseAddr().getChamMonimapCardUseAddrName();
            int visitCount = cardUseList.size();
            String cardUseRegion = first.getChamMonimapCardUseRegion();
            String cardUseUser = first.getChamMonimapCardUseUser();
            
            // visitMember 설정 로직
            Set<String> uniqueNames = cardUseList.stream()
                    .map(ChamMonimapCardUse::getChamMonimapCardUseName)
                    .collect(Collectors.toSet());
            
            String visitMember = uniqueNames.size() == 1
                    ? uniqueNames.iterator().next()
                    : String.format("%s 외 %d명", first.getChamMonimapCardUseName(), uniqueNames.size() - 1);
            
            int totalSum = cardUseList.stream()
                    .mapToInt(ChamMonimapCardUse::getChamMonimapCardUseAmount)
                    .sum();
            
            List<CardUseGroupedResponse> groupedResponses = cardUseList.stream()
                    .map(use -> new CardUseGroupedResponse(
                            use.getChamMonimapCardUseName(),
                            use.getAmountPerPerson(),
                            use.getChamMonimapCardUseMethod(),
                            use.getChamMonimapCardUseAmount(),
                            use.getChamMonimapCardUsePurpose(),
                            use.getChamMonimapCardUsePersonnel(),
                            use.getChamMonimapCardUseDate(),
                            use.getChamMonimapCardUseTime()
                    ))
                    .collect(Collectors.toList());
            
            LocalDate useDate = cardUseList.stream()
                    .map(ChamMonimapCardUse::getChamMonimapCardUseDate)
                    .max(Comparator.naturalOrder())
                    .orElse(null);
            
            String addrDetail = cardUseList.stream()
                    .map(item -> item.getCardUseAddr().getChamMonimapCardUseDetailAddr())
                    .findFirst()
                    .orElse("");
            
            String imageUrl = cardUseAddrRepository.findByImageUrl(addrId);
            
            // 댓글 내용 리스트
            List<ReplyResponse> replyList = repliesGroupedByAddrId
                    .getOrDefault(addrId, Collections.emptyList())
                    .stream()
                    .map(item -> {
                        Long replyId = item.getChamMonimapReplyId();
                        
                        List<String> imageUrls = replyImageGroup.getOrDefault(replyId, Collections.emptyList())
                                .stream()
                                .map(ChamMonimapReplyImage::getChamMonimapReplyImageUrl)
                                .collect(Collectors.toList());
                        
                        return new ReplyResponse(
                                replyId,
                                item.getChamMonimapReplyCont(),
                                item.getChamMonimapMember().getChamMonimapMemberName(),
                                item.getChamMonimapMember().getChamMonimapMemberImageUrl(),
                                item.getChamMonimapMember().getChamMonimapMemberEmail(),
                                imageUrls
                        );
                    })
                    .collect(Collectors.toList());
            
            // 응답 생성
            CardUseResponse response = new CardUseResponse(
                    addrName,
                    cardUseRegion,
                    cardUseUser,
                    visitCount,
                    visitMember,
                    totalSum,
                    addrDetail,
                    imageUrl,
                    addrId,
                    useDate,
                    groupedResponses,
                    replyList // 댓글 포함
            );
            
            resultMap.put(addrId, response);
        }

// 날짜 내림차순 정렬 후 반환
        Comparator<LocalDate> dateComparator = Comparator.nullsLast(Comparator.naturalOrder());
        return resultMap.entrySet()
                .stream()
                .sorted((item1, item2) -> {
                    LocalDate d1 = item1.getValue().getUseDate();
                    LocalDate d2 = item2.getValue().getUseDate();
                    return dateComparator.compare(d2, d1);
                })
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (a, b) -> a,
                        LinkedHashMap::new
                ));
    }
    
    
    @Override
    public ApiResponse insertCardUse(MultipartFile multipartFile) {
        QChamMonimapCardOwnerPosition cardOwnerPosition = chamMonimapCardOwnerPosition;
        QChamMonimapCardUseAddr cardUseAddr = chamMonimapCardUseAddr;
        List<CardOwnerPositionDto> cardOwnerPositionDtos = queryFactory
                .select(Projections.constructor(
                        CardOwnerPositionDto.class,
                        cardOwnerPosition.chamMonimapCardOwnerPositionId,
                        cardOwnerPosition.chamMonimapCardOwnerPositionName
                ))
                .from(cardOwnerPosition)
                .fetch();
        
        List<CardUseAddrDto> cardUseAddrDtos = queryFactory
                .select(Projections.constructor(
                        CardUseAddrDto.class,
                        cardUseAddr.chamMonimapCardUseAddrId,
                        cardUseAddr.chamMonimapCardUseAddrName,
                        cardUseAddr.chamMonimapCardUseDetailAddr
                ))
                .from(cardUseAddr)
                .fetch();
       
        
        try (InputStream is = multipartFile.getInputStream()) {
            Workbook workbook = WorkbookFactory.create(is);
            
            Sheet sheet = workbook.getSheetAt(0);
            String deleKeyValue = sheet.getRow(1).getCell(13).getStringCellValue();
            boolean exists = cardUseRepository.existsByChamMonimapCardUseDelkey(deleKeyValue);
            if(exists) {
                throw new CustomException("이미 존재하는 삭제키입니다.", 400);
            }
            for (Row row : sheet) {
                if (row.getRowNum() == 0) {
                    continue;
                }
                if (PoiUtil.isRowEmpty(row)) {
                    continue;
                }
                
                String cellValue = PoiUtil.getCellValue(row, 0); // 기관
                
                Optional<ChamMonimapCardOwnerPosition> existing = cardOwnerPositionRepository.findByCardOwnerPositionName(cellValue);
                
                if (existing.isEmpty()) {
                    ChamMonimapCardOwnerPosition newEntity = new ChamMonimapCardOwnerPosition(cellValue);
                    ChamMonimapCardOwnerPosition save = cardOwnerPositionRepository.save(newEntity);
                    cardOwnerPositionDtos.add(new CardOwnerPositionDto(save.getChamMonimapCardOwnerPositionId(), save.getChamMonimapCardOwnerPositionName()));
                }
                
                Long cardOwnerPositionId = cardOwnerPositionDtos.stream()
                        .filter(dto -> dto.getCardOwnerPositionName().equals(cellValue))
                        .map(CardOwnerPositionDto::getCardOwnerPositionId)
                        .findFirst()
                        .orElseThrow(() -> new CustomException("직책 이름에 해당하는 ID를 찾을 수 없습니다: " + cellValue, 400));
                
                ChamMonimapCardOwnerPosition newCardOwnerPosition = new ChamMonimapCardOwnerPosition(cardOwnerPositionId);
                
                
                Cell region = row.getCell(1);
                Cell userSell = row.getCell(2);
                String nameSell = PoiUtil.getCellValue(row, 3);
                Cell dateCell = row.getCell(4); // 집행일자
                Cell timeSell = row.getCell(5); // 시간
                Cell addrSell = row.getCell(6); // 사용장소
                String addrDetailValue = PoiUtil.getCellValue(row,7).trim(); // 상세주소
                Cell purposeSell = row.getCell(8); // 집행목적
                Cell personnelSell = row.getCell(9); //대상인원
                Cell amountCell = row.getCell(10); // 금액
                Cell methodCell = row.getCell(11); // 금액
                Cell remarkCell = row.getCell(12); // 비고
                Cell delKeyCell = row.getCell(13);
                
             
                
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
                
                ChamMonimapCardUseAddr cardUserAddr = cardUseAddrDtos.stream()
                        .filter(dto -> addrDetailValue.equals(dto.getCardUseDetailAddr().trim()))
                        .findFirst()
                        .map(dto -> new ChamMonimapCardUseAddr(dto.getCardUseAddrId()))
                        .orElseGet(() -> {
                            ChamMonimapCardUseAddr inserted = cardUseAddrService.insertCardUseAddr(
                                    new ChamMonimapCardUseAddr(addrSell.getStringCellValue(), addrDetailValue)
                            );
                            // 추가로 메모리에도 넣어줘야 이후 중복 insert 방지됨
                            cardUseAddrDtos.add(new CardUseAddrDto(
                                    inserted.getChamMonimapCardUseAddrId(),
                                    inserted.getChamMonimapCardUseAddrName(),
                                    inserted.getChamMonimapCardUseDetailAddr()
                            ));
                            return inserted;
                        });
                
                
                LocalDate dateValue = PoiUtil.getLocalDateFromCell(dateCell);
                LocalTime timeValue = PoiUtil.getLocalTimeFromCell(timeSell);
                
                ChamMonimapCardUse cardUse = new ChamMonimapCardUse(
                        newCardOwnerPosition,
                        cardUserAddr,
                        userSell.getStringCellValue(),
                        nameSell,
                        dateValue,
                        timeValue,
                        purposeSell.getStringCellValue(),
                        personnelStr,
                        amountCell.getNumericCellValue(),
                        methodCell.getStringCellValue(),
                        remarkCell.getStringCellValue(),
                        delKeyCell.getStringCellValue(),
                        region.getStringCellValue()
                );
                cardUseRepository.save(cardUse);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return new ApiResponse(200 , true,"성공");
    }
    
    @Override
    public ApiResponse deleteExcel(String deleteKey) {
        boolean exists = cardUseRepository.existsByChamMonimapCardUseDelkey(deleteKey);
        if (!exists) {
            throw new CustomException("존재하지 않는 삭제키 입니다. (대소문자 를 구분해 주세요)", 400);
        }
        cardUseRepository.deleteByCardUseDelkey(deleteKey);
        return new ApiResponse(200 , true,"삭제 되었습니다.");
    }
}
