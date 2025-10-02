package com.cham.caruse.service.impl;

import com.cham.advice.exception.ExcelException;
import com.cham.cardowner.entity.ChamMonimapCardOwnerPosition;
import com.cham.cardowner.repository.ChamMonimapCardOwnerPositionRepository;
import com.cham.carduseaddr.entity.ChamMonimapCardUseAddr;
import com.cham.carduseaddr.repository.ChamMonimapCardUseAddrRepository;
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
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
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
    
    private final ChamMonimapCardOwnerPositionRepository cardOwnerPositionRepository;
    
    @Override
    @Transactional(readOnly = true)
    public Map<Long, CardUseResponse> selectCardUse(CardUseConditionRequest request) {
        // 1) 데이터 조회
        List<ChamMonimapCardUse> cardUses  = cardUseRepository.findByCardUses(request);
        List<ChamMonimapReply> replies     = replyRepository.findByReplys();
        List<ChamMonimapReplyImage> images = replyImageRepository.findByReplyImages();
        
        // 2) 그룹핑
        Map<Long, List<ChamMonimapCardUse>> usesByAddrId = cardUses.stream()
                .collect(Collectors.groupingBy(u -> u.getCardUseAddr().getChamMonimapCardUseAddrId()));
        
        Map<Long, List<ChamMonimapReply>> repliesByAddrId = replies.stream()
                .collect(Collectors.groupingBy(r -> r.getChamMonimapCardUseAddr().getChamMonimapCardUseAddrId()));
        
        Map<Long, List<ChamMonimapReplyImage>> imagesByReplyId = images.stream()
                .collect(Collectors.groupingBy(img -> img.getChamMonimapReply().getChamMonimapReplyId()));
        
        // 3) 주소별 이미지 URL 벌크 조회 → Map<Long, String>
        Map<Long, String> imageUrlByAddrId = new LinkedHashMap<>();
        if (!usesByAddrId.isEmpty()) {
            List<ChamMonimapCardUseAddr> rows = cardUseAddrRepository.findImageUrlsByAddrIds(usesByAddrId.keySet());
            for (ChamMonimapCardUseAddr r : rows) {
                imageUrlByAddrId.put(r.getChamMonimapCardUseAddrId(), r.getChamMonimapCardUseImageUrl());
            }
        }
        // 4) 결과 맵 생성
        Map<Long, CardUseResponse> resultMap = new LinkedHashMap<>();
        
        
        for (Map.Entry<Long, List<ChamMonimapCardUse>> entry : usesByAddrId.entrySet()) {
            Long addrId = entry.getKey();
            List<ChamMonimapCardUse> list = entry.getValue();
            if (list == null || list.isEmpty()) {
                continue;
            }
            
            ChamMonimapCardUse first = list.get(0);
            
            // 방문자 합계/명단
            int totalSum = 0;
            Set<String> uniqueNames = new LinkedHashSet<>();
            for (ChamMonimapCardUse use : list) {
                totalSum += use.getChamMonimapCardUseAmount();
                uniqueNames.add(use.getChamMonimapCardUseName());
            }
            
            String visitMember = buildVisitMember(uniqueNames);
            
            
            // 상세 행 응답
            List<CardUseGroupedResponse> groupedResponses = list.stream()
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
                    .toList();
            
            String addrName   = first.getCardUseAddr().getChamMonimapCardUseAddrName();
            String addrDetail = first.getCardUseAddr().getChamMonimapCardUseDetailAddr();
            String region     = first.getChamMonimapCardUseRegion();
            String user       = first.getChamMonimapCardUseUser();
            
            String imageUrl = imageUrlByAddrId.get(addrId);
            
            // 댓글 응답
            List<ReplyResponse> replyList = repliesByAddrId.getOrDefault(addrId, Collections.emptyList())
                    .stream()
                    .map(rep -> {
                        Long rid = rep.getChamMonimapReplyId();
                        List<String> urls = imagesByReplyId.getOrDefault(rid, Collections.emptyList())
                                .stream()
                                .map(ChamMonimapReplyImage::getChamMonimapReplyImageUrl)
                                .toList();
                        return new ReplyResponse(
                                rid,
                                rep.getChamMonimapReplyCont(),
                                rep.getChamMonimapMember().getChamMonimapMemberName(),
                                rep.getChamMonimapMember().getChamMonimapMemberImageUrl(),
                                rep.getChamMonimapMember().getChamMonimapMemberEmail(),
                                urls
                        );
                    })
                    .toList();
            
            CardUseResponse resp = new CardUseResponse(
                    addrName,
                    region,
                    user,
                    list.size(),   // visitCount
                    visitMember,
                    totalSum,
                    addrDetail,
                    imageUrl,
                    addrId,
                    list.stream().map(ChamMonimapCardUse::getChamMonimapCardUseDate).max(Comparator.naturalOrder()).orElse(null),
                    groupedResponses,
                    replyList
            );
            
            resultMap.put(addrId, resp);
        }
        
        return resultMap.entrySet().stream()
                .sorted((e1, e2) -> {
                    LocalDate d1 = e1.getValue().getUseDate();
                    LocalDate d2 = e2.getValue().getUseDate();
                    if (d1 == null && d2 == null) return 0;
                    if (d1 == null) return 1;   // null 은 뒤로
                    if (d2 == null) return -1;
                    return d1.compareTo(d2);    // 오름차순
                })
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (a, b) -> a,
                        LinkedHashMap::new // 정렬 유지
                ));
    }
    
    @Override
    public Map<Long, CardUseResponse> selectCardUseDetail(String request) {
        // 1) 데이터 조회
        List<ChamMonimapCardUse> cardUses  = cardUseRepository.findByCardUsesDetail(request);
        List<ChamMonimapReply> replies     = replyRepository.findByReplys();
        List<ChamMonimapReplyImage> images = replyImageRepository.findByReplyImages();
        
        // 2) 그룹핑
        Map<Long, List<ChamMonimapCardUse>> usesByAddrId = cardUses.stream()
                .collect(Collectors.groupingBy(u -> u.getCardUseAddr().getChamMonimapCardUseAddrId()));
        
        Map<Long, List<ChamMonimapReply>> repliesByAddrId = replies.stream()
                .collect(Collectors.groupingBy(r -> r.getChamMonimapCardUseAddr().getChamMonimapCardUseAddrId()));
        
        Map<Long, List<ChamMonimapReplyImage>> imagesByReplyId = images.stream()
                .collect(Collectors.groupingBy(img -> img.getChamMonimapReply().getChamMonimapReplyId()));
        
        // 3) 주소별 이미지 URL 벌크 조회 → Map<Long, String>
        Map<Long, String> imageUrlByAddrId = new LinkedHashMap<>();
        if (!usesByAddrId.isEmpty()) {
            List<ChamMonimapCardUseAddr> rows = cardUseAddrRepository.findImageUrlsByAddrIds(usesByAddrId.keySet());
            for (ChamMonimapCardUseAddr r : rows) {
                imageUrlByAddrId.put(r.getChamMonimapCardUseAddrId(), r.getChamMonimapCardUseImageUrl());
            }
        }
        // 4) 결과 맵 생성
        Map<Long, CardUseResponse> resultMap = new LinkedHashMap<>();
        
        
        for (Map.Entry<Long, List<ChamMonimapCardUse>> entry : usesByAddrId.entrySet()) {
            Long addrId = entry.getKey();
            List<ChamMonimapCardUse> list = entry.getValue();
            if (list == null || list.isEmpty()) {
                continue;
            }
            
            ChamMonimapCardUse first = list.get(0);
            
            // 방문자 합계/명단
            int totalSum = 0;
            Set<String> uniqueNames = new LinkedHashSet<>();
            for (ChamMonimapCardUse use : list) {
                totalSum += use.getChamMonimapCardUseAmount();
                uniqueNames.add(use.getChamMonimapCardUseName());
            }
            
            String visitMember = buildVisitMember(uniqueNames);
            
            
            // 상세 행 응답
            List<CardUseGroupedResponse> groupedResponses = list.stream()
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
                    .toList();
            
            String addrName   = first.getCardUseAddr().getChamMonimapCardUseAddrName();
            String addrDetail = first.getCardUseAddr().getChamMonimapCardUseDetailAddr();
            String region     = first.getChamMonimapCardUseRegion();
            String user       = first.getChamMonimapCardUseUser();
            
            String imageUrl = imageUrlByAddrId.get(addrId);
            
            // 댓글 응답
            List<ReplyResponse> replyList = repliesByAddrId.getOrDefault(addrId, Collections.emptyList())
                    .stream()
                    .map(rep -> {
                        Long rid = rep.getChamMonimapReplyId();
                        List<String> urls = imagesByReplyId.getOrDefault(rid, Collections.emptyList())
                                .stream()
                                .map(ChamMonimapReplyImage::getChamMonimapReplyImageUrl)
                                .toList();
                        return new ReplyResponse(
                                rid,
                                rep.getChamMonimapReplyCont(),
                                rep.getChamMonimapMember().getChamMonimapMemberName(),
                                rep.getChamMonimapMember().getChamMonimapMemberImageUrl(),
                                rep.getChamMonimapMember().getChamMonimapMemberEmail(),
                                urls
                        );
                    })
                    .toList();
            
            CardUseResponse resp = new CardUseResponse(
                    addrName,
                    region,
                    user,
                    list.size(),   // visitCount
                    visitMember,
                    totalSum,
                    addrDetail,
                    imageUrl,
                    addrId,
                    list.stream().map(ChamMonimapCardUse::getChamMonimapCardUseDate).max(Comparator.naturalOrder()).orElse(null),
                    groupedResponses,
                    replyList
            );
            
            resultMap.put(addrId, resp);
        }
        
        return resultMap.entrySet().stream()
                .sorted((e1, e2) -> {
                    LocalDate d1 = e1.getValue().getUseDate();
                    LocalDate d2 = e2.getValue().getUseDate();
                    if (d1 == null && d2 == null) return 0;
                    if (d1 == null) return 1;   // null 은 뒤로
                    if (d2 == null) return -1;
                    return d1.compareTo(d2);    // 오름차순
                })
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (a, b) -> a,
                        LinkedHashMap::new // 정렬 유지
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
            // 4) 저장
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
        ChamMonimapCardUseAddr inserted = cardUseAddrRepository.save(
                new ChamMonimapCardUseAddr(addrName, addrDetail));
        // 캐시에도 반영
        cache.put(detailKey, new CardUseAddrDto(
                inserted.getChamMonimapCardUseAddrId(),
                inserted.getChamMonimapCardUseAddrName(),
                inserted.getChamMonimapCardUseDetailAddr()
        ));
        return inserted;
    }
    
    private String safeTrim(String s) {
        return s == null ? null : s.trim();
    }
    
    private String buildVisitMember(Set<String> names) {
        if (names == null || names.isEmpty()) {
            return "";
        }
        if (names.size() == 1) {
            return names.iterator().next();
        }
        String firstName = names.iterator().next();
        return String.format("%s 외 %d명", firstName, names.size() - 1);
    }
    
    
}
