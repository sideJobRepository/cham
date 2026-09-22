package com.cham.sendon.entity;

import com.cham.base.BaseData;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;

@Entity
@Table(name = "CHAM_MONIMAP_SENDON_SEND_HISTORY")
@Getter
public class ChamMonimapSendonSendHistory extends BaseData {
    
    
    // 자치연대 예산감시 샌드온 전송 이력 ID
    @Id
    @Column(name = "CHAM_MONIMAP_SENDON_SEND_HISTORY_ID")
    private Long chamMonimapSendonSendHistoryId;
    
    // 자치연대 예산감시 샌드온 전송 이력 내용
    @Column(name = "CHAM_MONIMAP_SENDON_SEND_HISTORY_CONT")
    private String chamMonimapSendonSendHistoryCont;
    
    // 자치연대 예산감시 샌드온 이력 메세지 고유값
    @Column(name = "CHAM_MONIMAP_SENDON_HISTORY_MSG_IDX")
    private String chamMonimapSendonHistoryMsgIdx;
    
    // 자치연대 예산감시 샌드온 이력 결과 코드
    @Column(name = "CHAM_MONIMAP_SENDON_HISTORY_RESULT_CODE")
    private String chamMonimapSendonHistoryResultCode;

}
