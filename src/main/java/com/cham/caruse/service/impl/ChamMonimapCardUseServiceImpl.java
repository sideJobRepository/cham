package com.cham.caruse.service.impl;

import com.cham.advice.exception.ExcelException;
import com.cham.cardowner.entity.ChamMonimapCardOwnerPosition;
import com.cham.cardowner.repository.ChamMonimapCardOwnerPositionRepository;
import com.cham.carduseaddr.entity.ChamMonimapCardUseAddr;
import com.cham.carduseaddr.repository.ChamMonimapCardUseAddrRepository;
import com.cham.caruse.dto.*;
import com.cham.caruse.entity.ChamMonimapCardUse;
import com.cham.caruse.repository.ChamMonimapCardUseRepository;
import com.cham.caruse.service.ChamMonimapCardUseService;
import com.cham.dto.request.CardUseConditionRequest;
import com.cham.dto.response.*;
import com.cham.region.entity.ChamMonimapRegion;
import com.cham.region.repository.ChamMonimapRegionRepository;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.cham.caruse.dto.RegionSummaryDto.toSummarySkeleton;


@RequiredArgsConstructor
@Service
@Transactional
public class ChamMonimapCardUseServiceImpl implements ChamMonimapCardUseService {
    
    private final ChamMonimapCardUseRepository cardUseRepository;
    
    private final ChamMonimapReplyRepository replyRepository;
    
    private final ChamMonimapReplyImageRepository replyImageRepository;
    
    private final ChamMonimapCardUseAddrRepository cardUseAddrRepository;
    
    private final ChamMonimapCardOwnerPositionRepository cardOwnerPositionRepository;
    
    private final ChamMonimapRegionRepository regionRepository;
    
    @Value("${kakao.clientId}")
    private String kakaoClientId;
    
    @Override
    @Transactional(readOnly = true)
    public CardUseAggregateResponse selectCardUse(CardUseConditionRequest request) {
        // 1) 데이터 조회
        List<ChamMonimapCardUse> cardUses  = cardUseRepository.findByCardUses(request);
        List<ChamMonimapReply> replies     = replyRepository.findByReplys();
        List<ChamMonimapReplyImage> images = replyImageRepository.findByReplyImages();
        
        // 2) 그룹핑 (기존과 동일)
        Map<Long, List<ChamMonimapCardUse>> usesByAddrId = cardUses.stream()
                .collect(Collectors.groupingBy(u -> u.getCardUseAddr().getChamMonimapCardUseAddrId()));
        Map<Long, List<ChamMonimapReply>> repliesByAddrId = replies.stream()
                .collect(Collectors.groupingBy(r -> r.getChamMonimapCardUseAddr().getChamMonimapCardUseAddrId()));
        Map<Long, List<ChamMonimapReplyImage>> imagesByReplyId = images.stream()
                .collect(Collectors.groupingBy(img -> img.getChamMonimapReply().getChamMonimapReplyId()));
        
        // 3) 이미지 URL 벌크 조회
        Map<Long, String> imageUrlByAddrId = new LinkedHashMap<>();
        if (!usesByAddrId.isEmpty()) {
            List<ChamMonimapCardUseAddr> rows = cardUseAddrRepository.findImageUrlsByAddrIds(usesByAddrId.keySet());
            for (ChamMonimapCardUseAddr r : rows) {
                imageUrlByAddrId.put(r.getChamMonimapCardUseAddrId(), r.getChamMonimapCardUseImageUrl());
            }
        }
        
        // 4) 주소별 응답 생성
        Map<Long, CardUseResponse> resultMap = new LinkedHashMap<>();
        for (Map.Entry<Long, List<ChamMonimapCardUse>> entry : usesByAddrId.entrySet()) {
            Long addrId = entry.getKey();
            List<ChamMonimapCardUse> list = entry.getValue();
            if (list == null || list.isEmpty()) continue;
            
            ChamMonimapCardUse first = list.get(0);
            
            // 방문자 합계 / 명단
            int totalSum = list.stream().mapToInt(ChamMonimapCardUse::getChamMonimapCardUseAmount).sum();
            Set<String> uniqueNames = list.stream()
                    .map(ChamMonimapCardUse::getChamMonimapCardUseName)
                    .collect(Collectors.toCollection(LinkedHashSet::new));
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
            String xValue = first.getCardUseAddr().getChamMonimapCardUseXValue();
            String yValue = first.getCardUseAddr().getChamMonimapCardUseYValue();
            String categoryName = first.getCardUseAddr().getChamMonimapCardUseCategoryName();
            CardUseResponse resp = new CardUseResponse(
                    first.getCardUseAddr().getChamMonimapCardUseAddrName(),
                    first.getChamMonimapCardUseRegion(),
                    first.getChamMonimapCardUseUser(),
                    list.size(),
                    visitMember,
                    totalSum,
                    first.getCardUseAddr().getChamMonimapCardUseDetailAddr(),
                    imageUrl,
                    addrId,
                    list.stream().map(ChamMonimapCardUse::getChamMonimapCardUseDate).max(Comparator.naturalOrder()).orElse(null),
                    xValue,
                    yValue,
                    categoryName,
                    groupedResponses,
                    replyList
            );
            
            resultMap.put(addrId, resp);
        }
        
        // 5) 지역별 요약 집계 (추가된 부분)
        RegionLevelsResponse regionLevelsResponse = summarizeByRegionLevels(cardUses);
        
        // 최종 응답 (주소별 상세 + 지역별 요약 같이 담기)
        return new CardUseAggregateResponse(resultMap, regionLevelsResponse);
    }
    
    @Override
    public CardUseAggregateResponse selectCardUseDetail(String request) {
        // 1) 데이터 조회
        List<ChamMonimapCardUse> cardUses  = cardUseRepository.findByCardUsesDetail(request);
        List<ChamMonimapReply> replies     = replyRepository.findByReplys();
        List<ChamMonimapReplyImage> images = replyImageRepository.findByReplyImages();
        
        // 2) 그룹핑 (기존과 동일)
        Map<Long, List<ChamMonimapCardUse>> usesByAddrId = cardUses.stream()
                .collect(Collectors.groupingBy(u -> u.getCardUseAddr().getChamMonimapCardUseAddrId()));
        Map<Long, List<ChamMonimapReply>> repliesByAddrId = replies.stream()
                .collect(Collectors.groupingBy(r -> r.getChamMonimapCardUseAddr().getChamMonimapCardUseAddrId()));
        Map<Long, List<ChamMonimapReplyImage>> imagesByReplyId = images.stream()
                .collect(Collectors.groupingBy(img -> img.getChamMonimapReply().getChamMonimapReplyId()));
        
        // 3) 이미지 URL 벌크 조회
        Map<Long, String> imageUrlByAddrId = new LinkedHashMap<>();
        if (!usesByAddrId.isEmpty()) {
            List<ChamMonimapCardUseAddr> rows = cardUseAddrRepository.findImageUrlsByAddrIds(usesByAddrId.keySet());
            for (ChamMonimapCardUseAddr r : rows) {
                imageUrlByAddrId.put(r.getChamMonimapCardUseAddrId(), r.getChamMonimapCardUseImageUrl());
            }
        }
        
        // 4) 주소별 응답 생성
        Map<Long, CardUseResponse> resultMap = new LinkedHashMap<>();
        for (Map.Entry<Long, List<ChamMonimapCardUse>> entry : usesByAddrId.entrySet()) {
            Long addrId = entry.getKey();
            List<ChamMonimapCardUse> list = entry.getValue();
            if (list == null || list.isEmpty()) continue;
            
            ChamMonimapCardUse first = list.get(0);
            
            // 방문자 합계 / 명단
            int totalSum = list.stream().mapToInt(ChamMonimapCardUse::getChamMonimapCardUseAmount).sum();
            Set<String> uniqueNames = list.stream()
                    .map(ChamMonimapCardUse::getChamMonimapCardUseName)
                    .collect(Collectors.toCollection(LinkedHashSet::new));
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
            String xValue = first.getCardUseAddr().getChamMonimapCardUseXValue();
            String yValue = first.getCardUseAddr().getChamMonimapCardUseYValue();
            String categoryName = first.getCardUseAddr().getChamMonimapCardUseCategoryName();
            CardUseResponse resp = new CardUseResponse(
                    first.getCardUseAddr().getChamMonimapCardUseAddrName(),
                    first.getChamMonimapCardUseRegion(),
                    first.getChamMonimapCardUseUser(),
                    list.size(),
                    visitMember,
                    totalSum,
                    first.getCardUseAddr().getChamMonimapCardUseDetailAddr(),
                    imageUrl,
                    addrId,
                    list.stream().map(ChamMonimapCardUse::getChamMonimapCardUseDate).max(Comparator.naturalOrder()).orElse(null),
                    xValue,
                    yValue,
                    categoryName,
                    groupedResponses,
                    replyList
            );
            
            resultMap.put(addrId, resp);
        }
        
        // 5) 지역별 요약 집계 (추가된 부분)
        RegionLevelsResponse regionLevelsResponse = summarizeByRegionLevels(cardUses);
        
        // 최종 응답 (주소별 상세 + 지역별 요약 같이 담기)
        return new CardUseAggregateResponse(resultMap, regionLevelsResponse);
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
        RestClient restClient = RestClient.create();
        KakaoAddressResponse body1 = restClient.get()
                .uri(uriBuilder ->
                        uriBuilder.scheme("https")
                                .host("dapi.kakao.com")
                                .path("/v2/local/search/address")
                                .queryParam("query", addrDetail)
                                .build()
                )
                .header("Authorization", "KakaoAK " + kakaoClientId)
                .retrieve()
                .toEntity(KakaoAddressResponse.class)
                .getBody();
        
        KakaoPlaceResponse body2 = restClient.get()
                .uri(uriBuilder ->
                        uriBuilder.scheme("https")
                                .host("dapi.kakao.com")
                                .path("/v2/local/search/keyword")
                                .queryParam("query", addrDetail)
                                .build()
                )
                .header("Authorization", "KakaoAK " + kakaoClientId)
                .retrieve()
                .toEntity(KakaoPlaceResponse.class)
                .getBody();
      
        Optional<KakaoAddressResponse.Document> docOpt = Optional.ofNullable(body1)
                .map(KakaoAddressResponse::getDocuments)
                .filter(list -> !list.isEmpty())
                .map(list -> list.get(0));
        
        
        Optional<KakaoPlaceResponse.Document> document = Optional.ofNullable(body2)
                .map(KakaoPlaceResponse::getDocuments)
                .flatMap(list -> list.stream()
                        .filter(item -> item.getPlaceName() != null
                                && item.getPlaceName().contains(addrName))
                        .findFirst()
                );
        
        ChamMonimapRegion dong = docOpt
                .map(KakaoAddressResponse.Document::getAddress)
                .map(a -> {
                    String r1 = a.getRegion_1depth_name(); // 대전
                    String r2 = a.getRegion_2depth_name(); // 서구
                    String r3 = a.getRegion_3depth_name(); // 탄방동
                    
                    String region = r1 + " " + r2 + " " + r3;
                    return saveRegionByName(region);
                })
                .orElse(null);
        
        String categoryName = document
                .map(KakaoPlaceResponse.Document::getCategoryName)
                .orElse(null);
        
        ChamMonimapCardUseAddr saved = cardUseAddrRepository.save(
                Optional.ofNullable(body1)
                        .map(KakaoAddressResponse::getDocuments)
                        .filter(list -> !list.isEmpty())
                        .map(list -> list.get(0))
                        .map(doc -> new ChamMonimapCardUseAddr(addrName, addrDetail, doc.getX(), doc.getY(),dong,categoryName))
                        .orElseGet(() -> new ChamMonimapCardUseAddr(addrName, addrDetail))
        );
        // 캐시에도 반영
        cache.put(detailKey, new CardUseAddrDto(
                saved.getChamMonimapCardUseAddrId(),
                saved.getChamMonimapCardUseAddrName(),
                saved.getChamMonimapCardUseDetailAddr()
        ));
        return saved;
    }
    
    
    
    public ChamMonimapRegion saveRegionByName(String query) {
        RestClient client = RestClient.create();
        
        // 1️주소명으로 좌표 검색
        KakaoAddressResponse addressResp = client.get()
                .uri(uriBuilder -> uriBuilder
                        .scheme("https")
                        .host("dapi.kakao.com")
                        .path("/v2/local/search/address")
                        .queryParam("query", query)
                        .build())
                .header("Authorization", "KakaoAK " + kakaoClientId)
                .retrieve()
                .toEntity(KakaoAddressResponse.class)
                .getBody();
        
        if (addressResp == null || addressResp.getDocuments().isEmpty()) {
            return null;
        }
        
        KakaoAddressResponse.Address address = addressResp.getDocuments().get(0).getAddress();
        String x = address.getX();
        String y = address.getY();

        // 2좌표 → 행정구역 조회
        KakaoRegionResponse regionResp = client.get()
                .uri(uriBuilder -> uriBuilder
                        .scheme("https")
                        .host("dapi.kakao.com")
                        .path("/v2/local/geo/coord2regioncode")
                        .queryParam("x", x)
                        .queryParam("y", y)
                        .build())
                .header("Authorization", "KakaoAK " + kakaoClientId)
                .retrieve()
                .toEntity(KakaoRegionResponse.class)
                .getBody();
        
        if (regionResp == null || regionResp.getDocuments().isEmpty()) {
            return null;
        }
        
        KakaoRegionResponse.Document doc = regionResp.getDocuments().get(0);

        // 0뎁스(도/광역시) 타입 자동 판별
        String depth0Name = doc.getRegion1depthName();
        String depth0Type;
        
        if (
                depth0Name.endsWith("시") ||
                        depth0Name.contains("광역시") ||
                        depth0Name.contains("특별시") ||
                        depth0Name.equals("제주특별자치도")
        ) {
            depth0Type = "METROPOLIS"; // 광역시, 특별시, 제주특별자치도
        } else {
            depth0Type = "DO"; // 도 단위는 GUN으로 저장
        }
        String depth1Name = doc.getRegion2depthName();
        String depth1Type = "CITY"; // 기본값
        
        if (depth1Name != null && !depth1Name.isBlank()) {
            // 마지막 단어 기준으로 판별 ("천안시 동남구" → "동남구")
            String[] parts = depth1Name.trim().split("\\s+");
            String last = parts[parts.length - 1];
            
            if (last.endsWith("구")) depth1Type = "GU";
            else if (last.endsWith("군")) depth1Type = "GUN";
            else if (last.endsWith("읍")) depth1Type = "EUP";
            else if (last.endsWith("면")) depth1Type = "MYEON";
            else if (last.endsWith("시")) depth1Type = "CITY";
        }
        
        //도, 시/구, 동 계층 저장
        ChamMonimapRegion province = saveOrGetRegion(
                null,
                depth0Name,
                depth0Type,   // 자동 판별된 타입 적용
                0,
                doc.getX(),
                doc.getY()
        );
        // 시/군/구/읍/면 자동 타입 저장
        ChamMonimapRegion city = saveOrGetRegion(
                province,
                depth1Name,
                depth1Type, // 동적으로 구분된 타입
                1,
                doc.getX(),
                doc.getY()
        );
        
        
        return saveOrGetRegion(
                city,
                doc.getRegion3depthName(),
                "DONG",
                2,
                doc.getX(),
                doc.getY()
        );
    }
    
    private ChamMonimapRegion saveOrGetRegion(ChamMonimapRegion parent, String name, String type, int depth, String x, String y) {
        if (name == null || name.isBlank()) return null;
        
        ChamMonimapRegion existing = regionRepository.findByNameAndDepth(name, depth);
        if (existing != null) return existing;
        
        ChamMonimapRegion region = new ChamMonimapRegion(parent, name, type, depth, x, y);
        return regionRepository.save(region);
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
    
    private RegionLevelsResponse summarizeByRegionLevels(List<ChamMonimapCardUse> cardUses) {
        Map<Long, RegionSummaryDto> byRegionId = new LinkedHashMap<>();
        
        BiConsumer<ChamMonimapRegion, Map<Long, RegionSummaryDto>> add = (r, map) -> {
            if (r == null) return;
            RegionSummaryDto dto = map.computeIfAbsent(r.getChamMonimapRegionId(), id -> toSummarySkeleton(r));
            dto.inc();
        };
        
        for (ChamMonimapCardUse use : cardUses) {
            ChamMonimapRegion dong = use.getCardUseAddr().getChamMonimapRegion();
            ChamMonimapRegion gu   = (dong != null) ? dong.getParent() : null;
            ChamMonimapRegion city = (gu   != null) ? gu.getParent()   : null;
            
            add.accept(city, byRegionId); // depth 0
            add.accept(gu,   byRegionId); // depth 1
            add.accept(dong, byRegionId); // depth 2
        }
        
        Comparator<RegionSummaryDto> order = Comparator
                .comparingInt(RegionSummaryDto::getDepth)
                .thenComparing(RegionSummaryDto::getPath);
        
        List<RegionSummaryDto> all = byRegionId.values().stream().sorted(order).toList();
        
        List<RegionSummaryDto> depth0 = all.stream().filter(d -> d.getDepth() == 0).toList();
        List<RegionSummaryDto> depth1 = all.stream().filter(d -> d.getDepth() == 1).toList();
        List<RegionSummaryDto> depth2 = all.stream().filter(d -> d.getDepth() == 2).toList();
        
        return RegionLevelsResponse.builder()
                .depth0(depth0)
                .depth1(depth1)
                .depth2(depth2)
                .build();
    }
    private String normalizeRegionName(String name) {
        if (name == null) return null;
        return name
                .replace("충청북도", "충북")
                .replace("충청남도", "충남")
                .replace("경상북도", "경북")
                .replace("경상남도", "경남")
                .replace("전라북도", "전북")
                .replace("전라남도", "전남")
                .replace("제주특별자치도", "제주")
                .replace("서울특별시", "서울")
                .replace("대전광역시", "대전")
                .replace("부산광역시", "부산")
                .replace("인천광역시", "인천");
    }
    
    
    
}
