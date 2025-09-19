package com.cham.caruse.service.impl;

import com.cham.advice.exception.ExcelException;
import com.cham.cardowner.entity.ChamMonimapCardOwnerPosition;
import com.cham.cardowner.repository.ChamMonimapCardOwnerPositionRepository;
import com.cham.carduseaddr.entity.ChamMonimapCardUseAddr;
import com.cham.carduseaddr.repository.ChamMonimapCardUseAddrRepository;
import com.cham.carduseaddr.service.ChamMonimapCardUseAddrService;
import com.cham.caruse.entity.ChamMonimapCardUse;
import com.cham.caruse.repository.ChamMonimapCardUseRepository;
import com.cham.caruse.service.ChamMonimapCardUseService;
import com.cham.dto.request.CardUseConditionRequest;
import com.cham.dto.response.*;
import com.cham.reply.entity.ChamMonimapReply;
import com.cham.reply.repository.ChamMonimapReplyRepository;
import com.cham.replyimage.entity.ChamMonimapReplyImage;
import com.cham.replyimage.repository.ChamMonimapReplyImageRepository;
import com.cham.util.ExcelColumns;
import com.cham.util.PoiUtil;
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
import java.util.function.Function;
import java.util.stream.Collectors;


@RequiredArgsConstructor
@Service
@Transactional
public class ChamMonimapCardUseServiceImpl implements ChamMonimapCardUseService {
    
    private final ChamMonimapCardUseRepository cardUseRepository;
    
    private final ChamMonimapReplyRepository replyRepository;
    
    private final ChamMonimapReplyImageRepository replyImageRepository;
    
    private final ChamMonimapCardUseAddrRepository cardUseAddrRepository;
    
    private final ChamMonimapCardUseAddrService cardUseAddrService;
    
    private final ChamMonimapCardOwnerPositionRepository cardOwnerPositionRepository;
    
    @Override
    public Map<Long, CardUseResponse> selectCardUse(CardUseConditionRequest request) {
        // 1. 카드사용 + 카드사용장소 + 댓글 조회
        List<ChamMonimapCardUse> cardUses = cardUseRepository.findByCardUses(request);
        
        List<ChamMonimapReply> replies = replyRepository.findByReplys();
        
        List<ChamMonimapReplyImage> replyImages = replyImageRepository.findByReplyImages();
        
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
        return resultMap.entrySet()
                .stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (a, b) -> a,
                        LinkedHashMap::new
                ));
    }
    
    
    @Override
    public ApiResponse insertCardUse(MultipartFile multipartFile) {
        Map<String, Long> positionIdByName = cardOwnerPositionRepository.findByCardOwnerPositionDtos().stream()
                .collect(Collectors.toMap(CardOwnerPositionDto::getCardOwnerPositionName,
                        CardOwnerPositionDto::getCardOwnerPositionId, (a, b) -> a, LinkedHashMap::new));
        
        Map<String, CardUseAddrDto> addrByDetail = cardUseAddrRepository.findByCardUseAddrDtos().stream()
                .collect(Collectors.toMap(dto -> safeTrim(dto.getCardUseDetailAddr()),
                        Function.identity(), (a, b) -> a, LinkedHashMap::new));
        
        // 1) 엑셀 열기
        try (InputStream is = multipartFile.getInputStream();
             Workbook workbook = WorkbookFactory.create(is)) {
            
            Sheet sheet = workbook.getSheetAt(0);
            
            // 2) 파일 레벨 삭제키 중복 체크 (시트의 2행 14열 = row1 col13)
            String deleteKey = PoiUtil.getString(sheet.getRow(1), ExcelColumns.DELKEY);
            if (deleteKey == null || deleteKey.isBlank()) {
                throw new ExcelException("삭제키가 비어 있습니다.", 400);
            }
            if (cardUseRepository.existsByChamMonimapCardUseDelkey(deleteKey)) {
                throw new ExcelException("이미 존재하는 삭제키입니다.", 400);
            }
            
            // 3) 본문 파싱 → 엔티티 리스트로 모아 배치 저장
            List<ChamMonimapCardUse> toInsert = new ArrayList<>();
            
            for (Row row : sheet) {
                int r = row.getRowNum();
                if (r == 0) {
                    continue;// 헤더 스킵
                }
                
                if (PoiUtil.isRowEmpty(row)) continue; // 빈행 스킵
                
                // (a) 기본 필드 파싱 (널 안전)
                String ownerPositionName = PoiUtil.getString(row, ExcelColumns.OWNER_POSITION);
                if (!StringUtils.hasText(ownerPositionName)) {
                    // 필수값 미기재 시 스킵/예외 중 택1. 여기선 예외.
                    throw new ExcelException("직책/기관명이 비어 있습니다. row=" + (r + 1), 400);
                }
                
                Long positionId = getOrCreatePositionId(ownerPositionName, positionIdByName);
                ChamMonimapCardOwnerPosition ownerPositionRef = new ChamMonimapCardOwnerPosition(positionId);
                
                String region = PoiUtil.getString(row, ExcelColumns.REGION);
                String userSell = PoiUtil.getString(row, ExcelColumns.USER_SELL);
                String nameSell = PoiUtil.getString(row, ExcelColumns.NAME_SELL);
                LocalDate useDate = PoiUtil.getLocalDateFromCell(row.getCell(ExcelColumns.DATE));
                LocalTime useTime = PoiUtil.getLocalTimeFromCell(row.getCell(ExcelColumns.TIME));
                
                String addrName   = PoiUtil.getString(row, ExcelColumns.ADDR_NAME);
                String addrDetail = safeTrim(PoiUtil.getString(row, ExcelColumns.ADDR_DETAIL));
                String purpose    = PoiUtil.getString(row, ExcelColumns.PURPOSE);
                String personnel  = PoiUtil.parsePersonnel(row.getCell(ExcelColumns.PERSONNEL));
                
                Double amount     = PoiUtil.getNumeric(row, ExcelColumns.AMOUNT); // 숫자/문자 혼용 안정화
                String method     = PoiUtil.getString(row, ExcelColumns.METHOD);
                String remark     = PoiUtil.getString(row, ExcelColumns.REMARK);
                
                // (b) 주소 upsert (상세주소 기준으로 동일)
                ChamMonimapCardUseAddr addrRef = getOrCreateAddr(addrName, addrDetail, addrByDetail);
                
                // (c) 행 단위 delKey: 파일레벨 deleteKey 고정 사용
                ChamMonimapCardUse entity = new ChamMonimapCardUse(
                        ownerPositionRef,
                        addrRef,
                        userSell,
                        nameSell,
                        useDate,
                        useTime,
                        purpose,
                        personnel,
                        amount != null ? amount : 0.0,
                        method,
                        remark,
                        deleteKey,
                        region
                );
                toInsert.add(entity);
            }
            // 4) 배치 저장
            if (!toInsert.isEmpty()) {
                cardUseRepository.saveAll(toInsert);
            }
            return new ApiResponse(200, true, "성공");
            
        } catch (IOException e) {
            throw new RuntimeException("엑셀 읽기 실패: " + e.getMessage(), e);
        }
    }
    
    @Override
    public ApiResponse deleteExcel(String deleteKey) {
        boolean exists = cardUseRepository.existsByChamMonimapCardUseDelkey(deleteKey);
        if (!exists) {
            throw new ExcelException("존재하지 않는 삭제키 입니다. (대소문자 를 구분해 주세요)", 400);
        }
        cardUseRepository.deleteByCardUseDelkey(deleteKey);
        return new ApiResponse(200 , true,"삭제 되었습니다.");
    }
    
    
    /** 직책/기관명 → ID 캐시 조회 후 없으면 생성 */
    private Long getOrCreatePositionId(String name, Map<String, Long> cache) {
        return cache.computeIfAbsent(name, key -> {
            ChamMonimapCardOwnerPosition saved = cardOwnerPositionRepository.save(
                    new ChamMonimapCardOwnerPosition(key));
            return saved.getChamMonimapCardOwnerPositionId();
        });
    }
    
    private ChamMonimapCardUseAddr getOrCreateAddr(String addrName, String addrDetail, Map<String, CardUseAddrDto> cache) {
        String detailKey = safeTrim(addrDetail);
        CardUseAddrDto hit = cache.get(detailKey);
        if (hit != null) {
            return new ChamMonimapCardUseAddr(hit.getCardUseAddrId());
        }
        // 없으면 생성
        ChamMonimapCardUseAddr inserted = cardUseAddrService.insertCardUseAddr(
                new ChamMonimapCardUseAddr(addrName, addrDetail));
        // 캐시에도 반영
        cache.put(detailKey, new CardUseAddrDto(
                inserted.getChamMonimapCardUseAddrId(),
                inserted.getChamMonimapCardUseAddrName(),
                inserted.getChamMonimapCardUseDetailAddr()
        ));
        return inserted;
    }
    
    private static String safeTrim(String s) {
        return s == null ? null : s.trim();
    }
    
}
