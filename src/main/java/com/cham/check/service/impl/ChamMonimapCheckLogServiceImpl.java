package com.cham.check.service.impl;

import com.cham.carduseaddr.entity.ChamMonimapCardUseAddr;
import com.cham.carduseaddr.repository.ChamMonimapCardUseAddrRepository;
import com.cham.check.dto.request.CheckLogSuspiciousPostRequest;
import com.cham.check.dto.request.CheckLogVisitedPostRequest;
import com.cham.check.dto.response.CheckLogGetResponse;
import com.cham.check.entity.ChamMonimapCheckLog;
import com.cham.check.repository.ChamMonimapCheckLogRepository;
import com.cham.check.service.ChamMonimapCheckLogService;
import com.cham.dto.response.ApiResponse;
import com.cham.member.entity.ChamMonimapMember;
import com.cham.member.repository.ChamMonimapMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class ChamMonimapCheckLogServiceImpl implements ChamMonimapCheckLogService {

    private final ChamMonimapCheckLogRepository chamMonimapCheckLogRepository;
    
    private final ChamMonimapMemberRepository monimapMemberRepository;
    
    private final ChamMonimapCardUseAddrRepository monimapCardUseAddrRepository;
    
    @Override
    public CheckLogGetResponse findByCheckAggregation(Long chamMonimapCardUseAddrId, Long memberId) {
        return chamMonimapCheckLogRepository.findByCheckAggregation(chamMonimapCardUseAddrId,memberId);
    }
    
    @Override
    public ApiResponse createCheckLogVisit(CheckLogVisitedPostRequest request) {
        //있는지 확인 부터
        Long addrId = request.getAddrId();
        Long memberId = request.getMemberId();
        ChamMonimapCheckLog checkLog = chamMonimapCheckLogRepository.findByCheckLog(memberId, addrId);
        //DB에 존재하지 않으면 insert
        if(checkLog == null){
            ChamMonimapMember chamMonimapMember = monimapMemberRepository.findById(memberId).orElseThrow(() -> new RuntimeException("존재하지 않는 회원 입니다."));
            ChamMonimapCardUseAddr chamMonimapCardUseAddr = monimapCardUseAddrRepository.findById(addrId).orElseThrow(() -> new RuntimeException("존재하지 않는 장소입니다."));
            chamMonimapCheckLogRepository.save(new ChamMonimapCheckLog(chamMonimapMember,chamMonimapCardUseAddr,request.getVisited(),null));
            return new ApiResponse(200, true, "저장 되었습니다.");
        }
        //있으면 기존 컬럼 값을 null로 만들고
        checkLog.modifyVisited(request.getVisited());
        return new ApiResponse(200,true,"수정 되었습니다.");
    }
    
    @Override
    public ApiResponse createCheckLogSuspicious(CheckLogSuspiciousPostRequest request) {
        Long addrId = request.getAddrId();
        Long memberId = request.getMemberId();
        ChamMonimapCheckLog checkLog = chamMonimapCheckLogRepository.findByCheckLog(memberId, addrId);
        //DB에 존재하지 않으면 insert
        if(checkLog == null){
            ChamMonimapMember chamMonimapMember = monimapMemberRepository.findById(memberId).orElseThrow(() -> new RuntimeException("존재하지 않는 회원 입니다."));
            ChamMonimapCardUseAddr chamMonimapCardUseAddr = monimapCardUseAddrRepository.findById(addrId).orElseThrow(() -> new RuntimeException("존재하지 않는 장소입니다."));
            chamMonimapCheckLogRepository.save(new ChamMonimapCheckLog(chamMonimapMember,chamMonimapCardUseAddr,null,request.getSuspicioused()));
            return new ApiResponse(200, true, "저장 되었습니다.");
        }
        checkLog.modifySuspicioused(request.getSuspicioused());
        return new ApiResponse(200,true,"수정 되었습니다.");
    }
}
