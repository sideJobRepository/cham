package com.cham.caruse.service.impl;

import com.cham.advice.exception.ExcelException;
import com.cham.cardowner.entity.ChamMonimapCardOwnerPosition;
import com.cham.cardowner.repository.ChamMonimapCardOwnerPositionRepository;
import com.cham.carduseaddr.entity.ChamMonimapCardUseAddr;
import com.cham.carduseaddr.repository.ChamMonimapCardUseAddrRepository;
import com.cham.caruse.dto.*;
import com.cham.caruse.entity.ChamMonimapCardUse;
import com.cham.caruse.repository.ChamMonimapCardUseRepository;
import com.cham.caruse.repository.dto.CardUseSummaryDto;
import com.cham.caruse.CardUseDefaults;
import com.cham.caruse.CardUseInsertOptions;
import com.cham.caruse.CardUseRow;
import com.cham.caruse.service.ChamMonimapCardUseService;
import com.cham.dto.request.CardUseConditionRequest;
import com.cham.dto.request.CardUseUploadDeleteKeyRequest;
import com.cham.dto.request.CardUseUploadPublicRequest;
import com.cham.dto.request.CleanupAddrRequest;
import com.cham.dto.request.CleanupNameRequest;
import com.cham.page.PageResponse;
import org.springframework.data.domain.Pageable;
import com.cham.dto.response.*;
import com.cham.region.entity.ChamMonimapRegion;
import com.cham.region.repository.ChamMonimapRegionRepository;
import com.cham.reply.entity.ChamMonimapReply;
import com.cham.reply.repository.ChamMonimapReplyRepository;
import com.cham.replyimage.entity.ChamMonimapReplyImage;
import com.cham.replyimage.repository.ChamMonimapReplyImageRepository;
import com.cham.theme.dto.response.ThemeGetResponse;
import com.cham.theme.enumeration.ChamMonimapThemeType;
import com.cham.theme.respotiroy.ChamMonimapThemeRepository;
import com.cham.theme.respotiroy.impl.ChamMonimapThemeRepositoryImpl;
import com.cham.util.ExcelColumns;
import com.cham.util.PoiUtil;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    
    private final ChamMonimapThemeRepository themeRepository;
    
    @Value("${kakao.clientId}")
    private String kakaoClientId;
    
    @Override
    @Transactional(readOnly = true)
    public CardUseAggregateResponse selectCardUse(CardUseConditionRequest request) {
        // 1) 데이터 조회
        List<ChamMonimapCardUse> cardUses = cardUseRepository.findByCardUses(request);
        List<CardUseSummaryDto> bySumTotalAmount = cardUseRepository.findBySumTotalAmount();
        List<ChamMonimapReply> replies = replyRepository.findByReplys();
        List<ChamMonimapReplyImage> images = replyImageRepository.findByReplyImages();
        List<ThemeGetResponse> themes = themeRepository.findByThemes();
        // 1-1) 이름 기준으로 합계 맵핑
        Map<Long, Integer> totalAmountById = bySumTotalAmount.stream()
                .collect(Collectors.toMap(
                        CardUseSummaryDto::getId,
                        CardUseSummaryDto::getTotalAmount,
                        (a, b) -> a // 중복 발생 시 첫 번째 값 유지
                ));
        // 2) 그룹핑 (기존 동일)
        Map<Long, List<ChamMonimapCardUse>> usesByAddrId = cardUses.stream()
                .collect(Collectors.groupingBy(u -> u.getCardUseAddr().getChamMonimapCardUseAddrId()));
        Map<Long, List<ChamMonimapReply>> repliesByAddrId = replies.stream()
                .collect(Collectors.groupingBy(r -> r.getChamMonimapCardUseAddr().getChamMonimapCardUseAddrId()));
        Map<Long, List<ChamMonimapReplyImage>> imagesByReplyId = images.stream()
                .collect(Collectors.groupingBy(img -> img.getChamMonimapReply().getChamMonimapReplyId()));

        // 3) 이미지 URL 벌크 조회 (그대로)
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
            String name = first.getCardUseAddr().getChamMonimapCardUseAddrName();
            
            //  sumTotalAmount 맵에서 매칭
            Integer totalSumFromDB = totalAmountById.getOrDefault(addrId, 0);
            
            // 방문자 합계 / 명단 (그대로)
            int localTotalSum = list.stream().mapToInt(ChamMonimapCardUse::getChamMonimapCardUseAmount).sum();
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
                            use.getChamMonimapCardUseTime(),
                            use.getChamMonimapCardUseRegion(),
                            use.getChamMonimapCardUseUser()
                    ))
                    .toList();
            
            String imageUrl = imageUrlByAddrId.get(addrId);
            
           
            // 댓글 응답 (그대로)
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
            String categoryName = first.getCardUseAddr().getChamMonimapCardUseCategoryName(); // 카테고리 이름
            String useUser = first.getChamMonimapCardUseUser();
            String cardUseName = first.getChamMonimapCardUseName(); // 이장우
            String region = first.getChamMonimapCardUseRegion(); // 대전
            String usePurpose = first.getChamMonimapCardUsePurpose();
            
            List<ChamMonimapCardUse> cardUseList = entry.getValue();
            
            String color = cardUseList.stream()
                    .map(use -> {
                        ChamMonimapCardOwnerPosition position = use.getChamMonimapCardOwnerPosition();
                        
                        // Theme 리스트 중 매칭되는 색상 찾기
                        return themes.stream()
                                .sorted(Comparator.comparing(theme -> !"INPUT".equals(theme.getThemeType()))) // INPUT 우선
                                .filter(theme -> {
                                    if ("INPUT".equals(theme.getThemeType())) {
                                        return matchesThemeInput(theme, categoryName, useUser, cardUseName, region, usePurpose);
                                    } else if ("OWNER".equals(theme.getThemeType())) {
                                        return Objects.equals(theme.getTargetId(), position.getChamMonimapCardOwnerPositionId());
                                    }
                                    return false;
                                })
                                .findFirst()
                                .map(ThemeGetResponse::getColor)
                                .orElse(null);
                    })
                    .filter(Objects::nonNull) // color가 null인 항목 제외
                    .findFirst()
                    .orElse(null); // 전체 결과도 null 허용
            //  DB 집계 값이 우선, 없으면 기존 합계 사용
            int totalSum = (totalSumFromDB != null && totalSumFromDB > 0)
                    ? totalSumFromDB
                    : localTotalSum;
            
            CardUseResponse resp = new CardUseResponse(
                    name,
                    first.getChamMonimapCardUseRegion(),
                    first.getChamMonimapCardUseUser(),
                    list.size(),
                    visitMember,
                    totalSum, // 여기에 최종합계 반영
                    first.getCardUseAddr().getChamMonimapCardUseDetailAddr(),
                    imageUrl,
                    addrId,
                    list.stream().map(ChamMonimapCardUse::getChamMonimapCardUseDate).max(Comparator.naturalOrder()).orElse(null),
                    xValue,
                    yValue,
                    categoryName,
                    color,
                    groupedResponses,
                    replyList
            );
            
            resultMap.put(addrId, resp);
        }

        // 5) 지역별 요약 집계
        RegionLevelsResponse regionLevelsResponse = summarizeByRegionLevels(cardUses);

        // 최종 응답
        return new CardUseAggregateResponse(resultMap, regionLevelsResponse);
    }
    
    @Override
    public CardUseAggregateResponse selectCardUseDetail(String request) {
        // 1) 데이터 조회
        List<ChamMonimapCardUse> cardUses  = cardUseRepository.findByCardUsesDetail(request);
        List<ChamMonimapReply> replies     = replyRepository.findByReplys();
        List<ChamMonimapReplyImage> images = replyImageRepository.findByReplyImages();
        List<ThemeGetResponse> themes = themeRepository.findByThemes();
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
                            use.getChamMonimapCardUseTime(),
                            use.getChamMonimapCardUseRegion(),
                            use.getChamMonimapCardUseUser()
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
            String categoryName = first.getCardUseAddr().getChamMonimapCardUseCategoryName(); // 카테고리 이름
            String useUser = first.getChamMonimapCardUseUser();
            String cardUseName = first.getChamMonimapCardUseName(); // 이장우
            String region = first.getChamMonimapCardUseRegion(); // 대전
            String usePurpose = first.getChamMonimapCardUsePurpose();
            
            List<ChamMonimapCardUse> cardUseList = entry.getValue();
            
            String color = cardUseList.stream()
                    .map(use -> {
                        ChamMonimapCardOwnerPosition position = use.getChamMonimapCardOwnerPosition();
                        
                        // Theme 리스트 중 매칭되는 색상 찾기
                        return themes.stream()
                                .sorted(Comparator.comparing(theme -> !"INPUT".equals(theme.getThemeType()))) // INPUT 우선
                                .filter(theme -> {
                                    if ("INPUT".equals(theme.getThemeType())) {
                                        return matchesThemeInput(theme, categoryName, useUser, cardUseName, region, usePurpose);
                                    } else if ("OWNER".equals(theme.getThemeType())) {
                                        return Objects.equals(theme.getTargetId(), position.getChamMonimapCardOwnerPositionId());
                                    }
                                    return false;
                                })
                                .findFirst()
                                .map(ThemeGetResponse::getColor)
                                .orElse(null);
                    })
                    .filter(Objects::nonNull) // color가 null인 항목 제외
                    .findFirst()
                    .orElse(null); // 전체 결과도 null 허용
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
                    color,
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

            // 3) 본문 파싱 → 저장
            List<CardUseRow> rows = parsePositional(sheet);
            if (!rows.isEmpty()) {
                insertRows(rows, deleteKey, CardUseInsertOptions.MANUAL_UPLOAD);
            }
            return new ApiResponse(200, true, "성공");

        } catch (IOException e) {
            throw new RuntimeException("엑셀 읽기 실패: " + e.getMessage(), e);
        }
    }

    /** 수동 업로드 양식(ExcelColumns 위치 고정)을 읽는다. 첫 줄은 헤더라 건너뛴다. */
    private List<CardUseRow> parsePositional(Sheet sheet) {
        List<CardUseRow> rows = new ArrayList<>();

        for (Row row : sheet) {
            int r = row.getRowNum();
            if (r == 0) {
                continue;// 헤더 스킵
            }

            if (PoiUtil.isRowEmpty(row)) continue; // 빈행 스킵

            String ownerPositionName = PoiUtil.getString(row, ExcelColumns.OWNER_POSITION);
            if (!StringUtils.hasText(ownerPositionName)) {
                // 필수값 미기재 시 스킵/예외 중 택1. 여기선 예외.
                throw new ExcelException("직책/기관명이 비어 있습니다. row=" + (r + 1), 400);
            }

            rows.add(new CardUseRow(
                    ownerPositionName,
                    PoiUtil.getString(row, ExcelColumns.REGION),
                    PoiUtil.getString(row, ExcelColumns.USER_SELL),
                    PoiUtil.getString(row, ExcelColumns.NAME_SELL),
                    PoiUtil.getLocalDateFromCell(row.getCell(ExcelColumns.DATE)),
                    PoiUtil.getLocalTimeFromCell(row.getCell(ExcelColumns.TIME)),
                    PoiUtil.getString(row, ExcelColumns.ADDR_NAME),
                    PoiUtil.getString(row, ExcelColumns.ADDR_DETAIL),
                    PoiUtil.getString(row, ExcelColumns.PURPOSE),
                    PoiUtil.parsePersonnel(row.getCell(ExcelColumns.PERSONNEL)),
                    PoiUtil.getNumeric(row, ExcelColumns.AMOUNT), // 숫자/문자 혼용 안정화
                    PoiUtil.getString(row, ExcelColumns.METHOD),
                    PoiUtil.getString(row, ExcelColumns.REMARK),
                    r + 1,
                    sheet.getSheetName()
            ));
        }
        return rows;
    }

    @Override
    public int insertRows(List<CardUseRow> rows, String deleteKey, CardUseInsertOptions options) {
        if (rows == null || rows.isEmpty()) {
            throw new ExcelException("반영할 행이 없습니다.", 400);
        }
        if (!StringUtils.hasText(deleteKey)) {
            throw new ExcelException("삭제키가 비어 있습니다.", 400);
        }
        if (cardUseRepository.existsByChamMonimapCardUseDelkey(deleteKey)) {
            throw new ExcelException("이미 존재하는 삭제키입니다.", 400);
        }

        Map<String, Long> positionIdByName = cardOwnerPositionRepository.findByCardOwnerPositionDtos().stream()
                .collect(Collectors.toMap(CardOwnerPositionDto::getCardOwnerPositionName,
                        CardOwnerPositionDto::getCardOwnerPositionId, (a, b) -> a, LinkedHashMap::new));

        Map<String, CardUseAddrDto> addrByDetail = cardUseAddrRepository.findByCardUseAddrDtos().stream()
                .collect(Collectors.toMap(dto -> safeTrim(dto.getCardUseDetailAddr()),
                        Function.identity(), (a, b) -> a, LinkedHashMap::new));

        // 이번 저장 안에서 같은 상세주소는 카카오를 다시 부르지 않는다.
        // 단체장 자료는 장소가 없어 모든 줄이 같은 자리값 주소라, 이게 없으면 줄 수만큼 호출이 나간다.
        Map<String, ChamMonimapCardUseAddr> addrThisRun = new HashMap<>();

        List<ChamMonimapCardUse> toInsert = new ArrayList<>();

        for (CardUseRow row : rows) {
            String ownerPositionName = safeTrim(row.ownerPosition());
            if (!StringUtils.hasText(ownerPositionName)) {
                throw new ExcelException("직책/기관명이 비어 있습니다. row=" + row.sourceRowNum(), 400);
            }
            Long positionId = options.allowCreatePosition()
                    ? getOrCreatePositionId(ownerPositionName, positionIdByName)
                    : Optional.ofNullable(positionIdByName.get(ownerPositionName))
                            .orElseThrow(() -> new ExcelException(
                                    "등록되지 않은 직책입니다: '" + ownerPositionName + "' (등록된 직책: "
                                            + String.join(", ", positionIdByName.keySet()) + ")", 400));
            ChamMonimapCardOwnerPosition ownerPositionRef = new ChamMonimapCardOwnerPosition(positionId);

            String addrName = row.addrName();
            if (!StringUtils.hasText(addrName)) {
                addrName = CardUseDefaults.ADDR_NAME;
            }
            String addrDetail = safeTrim(row.addrDetail());
            if (!StringUtils.hasText(addrDetail)) {
                addrDetail = CardUseDefaults.DETAIL_ADDR;
            }
            String personnel = row.personnel();
            if (!StringUtils.hasText(personnel)) {
                personnel = "1";
            }

            // 주소 upsert (상세주소 기준으로 동일)
            final String name = addrName;
            ChamMonimapCardUseAddr addrRef = addrThisRun.computeIfAbsent(addrDetail,
                    detail -> getOrCreateAddr(name, detail, addrByDetail));

            // 행 단위 delKey: 파일레벨 deleteKey 고정 사용
            toInsert.add(new ChamMonimapCardUse(
                    ownerPositionRef,
                    addrRef,
                    row.user(),
                    row.name(),
                    row.date(),
                    row.time(),
                    row.purpose(),
                    personnel,
                    row.amount() != null ? row.amount() : 0.0,
                    row.method(),
                    row.remark(),
                    deleteKey,
                    row.region(),
                    options.isPublic()
            ));
        }

        cardUseRepository.saveAll(toInsert);
        return toInsert.size();
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

    @Override
    @Transactional(readOnly = true)
    public List<CardUseUploadResponse> selectCardUseUploads() {
        List<CardUseUploadResponse> uploads = cardUseRepository.findCardUseUploads();

        // 명단은 업로드마다 따로 조회하지 않고 전체를 한 번에 받아 삭제키로 묶는다.
        // 업로드 수만큼 쿼리가 늘어나는 것을 피하려는 것이다.
        Map<String, List<CardUseUploadMemberResponse>> membersByDeleteKey =
                cardUseRepository.findCardUseUploadMembers().stream()
                        .collect(Collectors.groupingBy(
                                CardUseUploadMemberResponse::getDeleteKey,
                                LinkedHashMap::new,
                                Collectors.toList()));

        uploads.forEach(upload ->
                upload.setMembers(membersByDeleteKey.getOrDefault(upload.getDeleteKey(), List.of())));

        return uploads;
    }

    @Override
    public ApiResponse modifyCardUseUploadPublic(CardUseUploadPublicRequest request) {
        String deleteKey = request.getDeleteKey();
        boolean isPublic = Boolean.TRUE.equals(request.getIsPublic());

        if (!cardUseRepository.existsByChamMonimapCardUseDelkey(deleteKey)) {
            throw new ExcelException("존재하지 않는 삭제키 입니다. (대소문자 를 구분해 주세요)", 400);
        }

        long affected = cardUseRepository.updatePublicByDelkey(deleteKey, isPublic);

        return new ApiResponse(200, true,
                (isPublic ? "공개" : "비공개") + "로 전환했습니다. (" + affected + "건)");
    }

    @Override
    public ApiResponse modifyCardUseUploadDeleteKey(CardUseUploadDeleteKeyRequest request) {
        String deleteKey = request.getDeleteKey();
        String newDeleteKey = request.getNewDeleteKey().trim();

        if (!StringUtils.hasText(newDeleteKey)) {
            throw new ExcelException("새 삭제키가 비어 있습니다.", 400);
        }
        if (!cardUseRepository.existsByChamMonimapCardUseDelkey(deleteKey)) {
            throw new ExcelException("존재하지 않는 삭제키 입니다. (대소문자 를 구분해 주세요)", 400);
        }
        if (newDeleteKey.equals(deleteKey)) {
            throw new ExcelException("이전과 같은 삭제키입니다.", 400);
        }
        // 이미 쓰는 이름으로 바꾸면 서로 다른 업로드 두 건이 한 덩어리로 합쳐진다.
        // 그때부터는 삭제도 공개 전환도 둘을 갈라서 할 수 없으니 막는다.
        if (cardUseRepository.existsByChamMonimapCardUseDelkey(newDeleteKey)) {
            throw new ExcelException("이미 존재하는 삭제키입니다.", 400);
        }

        long affected = cardUseRepository.updateDelkey(deleteKey, newDeleteKey);

        return new ApiResponse(200, true,
                "삭제키를 '" + newDeleteKey + "' 로 변경했습니다. (" + affected + "건)");
    }

    // 업로드가 읽는 열 순서(ExcelColumns)를 그대로 따른다. 받은 파일을 고쳐서 다시 올릴 수 있어야 한다.
    private static final String[] EXPORT_HEADERS = {
            "기관/직책", "지역", "사용자", "이름", "집행일자", "시간", "사용장소명",
            "상세주소", "집행목적", "대상인원", "금액", "결제방법", "비고", "삭제키"
    };

    @Override
    @Transactional(readOnly = true)
    public byte[] exportCardUseUpload(String deleteKey) {
        List<ChamMonimapCardUse> uses = cardUseRepository.findByDelkeyForExport(deleteKey);
        if (uses.isEmpty()) {
            throw new ExcelException("존재하지 않는 삭제키 입니다. (대소문자 를 구분해 주세요)", 400);
        }

        // 행이 많아질 수 있어 SXSSF 로 쓴다. 100행만 메모리에 두고 나머지는 디스크로 흘린다.
        // 디스크로 흘린 임시파일은 close() 가 같이 지운다(POI 5.4 부터 dispose 는 없어졌다).
        try (SXSSFWorkbook workbook = new SXSSFWorkbook(100);
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("업무추진비");

            Row header = sheet.createRow(0);
            for (int i = 0; i < EXPORT_HEADERS.length; i++) {
                header.createCell(i).setCellValue(EXPORT_HEADERS[i]);
            }

            int rowNum = 1;
            for (ChamMonimapCardUse use : uses) {
                Row row = sheet.createRow(rowNum++);

                ChamMonimapCardUseAddr addr = use.getCardUseAddr();
                ChamMonimapCardOwnerPosition position = use.getChamMonimapCardOwnerPosition();

                setText(row, ExcelColumns.OWNER_POSITION,
                        position == null ? null : position.getChamMonimapCardOwnerPositionName());
                setText(row, ExcelColumns.REGION, use.getChamMonimapCardUseRegion());
                setText(row, ExcelColumns.USER_SELL, use.getChamMonimapCardUseUser());
                setText(row, ExcelColumns.NAME_SELL, use.getChamMonimapCardUseName());

                // 날짜·시간은 문자열로 쓴다. 업로드 쪽 파서가 이 형식을 그대로 읽는다.
                setText(row, ExcelColumns.DATE,
                        use.getChamMonimapCardUseDate() == null ? null
                                : use.getChamMonimapCardUseDate().toString());
                setText(row, ExcelColumns.TIME,
                        use.getChamMonimapCardUseTime() == null ? null
                                : use.getChamMonimapCardUseTime().toString());

                setText(row, ExcelColumns.ADDR_NAME,
                        addr == null ? null : addr.getChamMonimapCardUseAddrName());
                setText(row, ExcelColumns.ADDR_DETAIL,
                        addr == null ? null : addr.getChamMonimapCardUseDetailAddr());
                setText(row, ExcelColumns.PURPOSE, use.getChamMonimapCardUsePurpose());
                setText(row, ExcelColumns.PERSONNEL, use.getChamMonimapCardUsePersonnel());

                if (use.getChamMonimapCardUseAmount() == null) {
                    setText(row, ExcelColumns.AMOUNT, null);
                } else {
                    row.createCell(ExcelColumns.AMOUNT).setCellValue(use.getChamMonimapCardUseAmount());
                }

                setText(row, ExcelColumns.METHOD, use.getChamMonimapCardUseMethod());
                setText(row, ExcelColumns.REMARK, use.getChamMonimapCardUseRemark());
                setText(row, ExcelColumns.DELKEY, use.getChamMonimapCardUseDelkey());
            }

            workbook.write(out);
            return out.toByteArray();

        } catch (IOException e) {
            throw new ExcelException("엑셀을 만드는 중 오류가 발생했습니다.", 400);
        }
    }

    // 빈 값도 빈 문자열로 채운다. 칸을 아예 비워두면 다시 올릴 때 null 이 되어 터지는 자리가 있다.
    private void setText(Row row, int column, String value) {
        row.createCell(column).setCellValue(value == null ? "" : value);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<CleanupAddrResponse> selectCleanupAddrs(String keyword, boolean onlyIssues, Pageable pageable) {
        return PageResponse.from(cardUseRepository.findCleanupAddrs(safeTrim(keyword), onlyIssues, pageable));
    }

    @Override
    public ApiResponse modifyCleanupAddr(CleanupAddrRequest request) {
        String addrName = safeTrim(request.getAddrName());
        String detailAddr = safeTrim(request.getDetailAddr());

        ChamMonimapCardUseAddr addr = cardUseAddrRepository.findById(request.getAddrId())
                .orElseThrow(() -> new ExcelException("존재하지 않는 장소입니다.", 400));

        boolean addrChanged = !detailAddr.equals(safeTrim(addr.getChamMonimapCardUseDetailAddr()));

        String x = null;
        String y = null;
        ChamMonimapRegion region = null;

        // 주소가 그대로면 좌표를 다시 찾지 않는다. 카카오 호출을 아끼는 것도 있지만,
        // 가게 이름만 고치려던 것이 핀 위치까지 움직이면 곤란하다.
        if (addrChanged) {
            KakaoAddressResponse.Document document = searchAddrDocument(detailAddr);
            if (document != null) {
                x = document.getX();
                y = document.getY();
                region = Optional.ofNullable(document.getAddress())
                        .map(a -> saveRegionByName(
                                a.getRegion_1depth_name() + " "
                                        + a.getRegion_2depth_name() + " "
                                        + a.getRegion_3depth_name()))
                        .orElse(null);
            }
        }

        addr.modifyAddr(addrName, detailAddr, x, y, region);

        if (!addrChanged) {
            return new ApiResponse(200, true, "장소를 수정했습니다.");
        }
        if (x == null) {
            return new ApiResponse(200, true,
                    "장소를 수정했습니다. 다만 새 주소로 좌표를 찾지 못해 이전 위치를 그대로 둡니다.");
        }
        return new ApiResponse(200, true, "장소를 수정하고 좌표를 다시 찾았습니다.");
    }

    // 업로드 때 쓰는 카카오 주소검색과 같은 API 다. 여기서는 첫 결과만 본다.
    private KakaoAddressResponse.Document searchAddrDocument(String detailAddr) {
        KakaoAddressResponse body = RestClient.create().get()
                .uri(uriBuilder -> uriBuilder.scheme("https")
                        .host("dapi.kakao.com")
                        .path("/v2/local/search/address")
                        .queryParam("query", detailAddr)
                        .build())
                .header("Authorization", "KakaoAK " + kakaoClientId)
                .retrieve()
                .toEntity(KakaoAddressResponse.class)
                .getBody();

        return Optional.ofNullable(body)
                .map(KakaoAddressResponse::getDocuments)
                .filter(list -> !list.isEmpty())
                .map(list -> list.get(0))
                .orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<CleanupNameResponse> selectCleanupNames(String keyword, boolean onlyIssues, Pageable pageable) {
        return PageResponse.from(cardUseRepository.findCleanupNames(safeTrim(keyword), onlyIssues, pageable));
    }

    @Override
    public ApiResponse modifyCleanupName(CleanupNameRequest request) {
        String oldName = request.getOldName();
        String newName = safeTrim(request.getNewName());

        if (!StringUtils.hasText(newName)) {
            throw new ExcelException("새 이름이 비어 있습니다.", 400);
        }
        if (newName.equals(oldName)) {
            throw new ExcelException("이전과 같은 이름입니다.", 400);
        }

        long affected = cardUseRepository.updateCardUseName(oldName, newName);
        if (affected == 0) {
            throw new ExcelException("해당 이름으로 된 자료가 없습니다.", 400);
        }

        return new ApiResponse(200, true,
                "'" + newName + "' 로 변경했습니다. (" + affected + "건)");
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
        RestClient restClient = RestClient.create();
        
        // 카카오 주소 API로 좌표 조회
        KakaoAddressResponse body1 = restClient.get()
                .uri(uriBuilder -> uriBuilder.scheme("https")
                        .host("dapi.kakao.com")
                        .path("/v2/local/search/address")
                        .queryParam("query", addrDetail)
                        .build())
                .header("Authorization", "KakaoAK " + kakaoClientId)
                .retrieve()
                .toEntity(KakaoAddressResponse.class)
                .getBody();
        
        // 주소 문서
        Optional<KakaoAddressResponse.Document> docOpt = Optional.ofNullable(body1)
                .map(KakaoAddressResponse::getDocuments)
                .filter(list -> !list.isEmpty())
                .map(list -> list.get(0));
        
        // 좌표 추출
        String x = docOpt.map(KakaoAddressResponse.Document::getX).orElse(null);
        String y = docOpt.map(KakaoAddressResponse.Document::getY).orElse(null);
        
        //  좌표 기반 캐시 키 생성
        String coordKey = (x != null && y != null)
                ? (x + "," + y)
                : safeTrim(addrDetail);
        
        //  캐시 hit 체크
        CardUseAddrDto hit = cache.get(coordKey);
        if (hit != null && hit.getCardUseAddrId() != null) {
            return new ChamMonimapCardUseAddr(hit.getCardUseAddrId());
        }
        
        //  DB 중복 체크 (이미 저장된 동일 좌표 있는지)
        if (x != null && y != null) {
            Optional<ChamMonimapCardUseAddr> existing = cardUseAddrRepository
                    .findByXValueAndYValue(x, y, 0.0005); //약 50m범위
            if (existing.isPresent()) {
                ChamMonimapCardUseAddr found = existing.get();
                cache.put(coordKey, new CardUseAddrDto(
                        found.getChamMonimapCardUseAddrId(),
                        found.getChamMonimapCardUseAddrName(),
                        found.getChamMonimapCardUseDetailAddr()
                ));
                return found; // 이미 존재 → 재사용
            }
        }
        
        // 📍 4카카오 키워드 API로 카테고리 조회
        KakaoPlaceResponse body2 = restClient.get()
                .uri(uriBuilder -> uriBuilder.scheme("https")
                        .host("dapi.kakao.com")
                        .path("/v2/local/search/keyword")
                        .queryParam("query", addrDetail)
                        .build())
                .header("Authorization", "KakaoAK " + kakaoClientId)
                .retrieve()
                .toEntity(KakaoPlaceResponse.class)
                .getBody();
        
        Optional<KakaoPlaceResponse.Document> placeOpt = Optional.ofNullable(body2)
                .map(KakaoPlaceResponse::getDocuments)
                .flatMap(list -> list.stream()
                        .filter(item -> item.getPlaceName() != null
                                && item.getPlaceName().contains(addrName))
                        .findFirst());
        
        String categoryName = placeOpt.map(KakaoPlaceResponse.Document::getCategoryName)
                .orElse(null);
        
        // 📍  Region 생성
        ChamMonimapRegion dong = docOpt
                .map(KakaoAddressResponse.Document::getAddress)
                .map(a -> {
                    String r1 = a.getRegion_1depth_name();
                    String r2 = a.getRegion_2depth_name();
                    String r3 = a.getRegion_3depth_name();
                    String region = r1 + " " + r2 + " " + r3;
                    return saveRegionByName(region);
                })
                .orElse(null);
        
        // 📍  새 주소 DB 저장
        ChamMonimapCardUseAddr saved = cardUseAddrRepository.save(
                new ChamMonimapCardUseAddr(addrName, addrDetail, x, y, dong, categoryName)
        );
        
        // 캐시 갱신 (좌표 기준)
        cache.put(coordKey, new CardUseAddrDto(
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
    
    private boolean matchesThemeInput(ThemeGetResponse theme,
                                      String categoryName,
                                      String useUser,
                                      String cardUseName,
                                      String region,
                                      String usePurpose) {
        String input = theme.getInputValue();
        if (input == null || input.isBlank()) return false;
        
        // 비교 안정화 (공백/제어문자 제거)
        String normalizedInput = input.strip().replaceAll("\\s+", "");
        return Stream.of(categoryName, useUser, cardUseName, region, usePurpose)
                .filter(Objects::nonNull)
                .map(v -> v.strip().replaceAll("\\s+", ""))
                .anyMatch(v -> v.contains(normalizedInput));
    }
}
