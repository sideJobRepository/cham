package com.cham.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReplyCreateRequest {
    
    
    private Long cardUseAddrId;
    
    private Long memberId;
    
    private String replyCont;
    
    private List<MultipartFile> fileList;
    
    
    public ReplyCreateRequest(Long cardUseAddrId, Long memberId, String replyCont) {
        this.cardUseAddrId = cardUseAddrId;
        this.memberId = memberId;
        this.replyCont = replyCont;
    }
    
    public List<MultipartFile> getFileList() {
        if(this.fileList == null){
            this.fileList = new ArrayList<>();
        }
        return fileList;
    }
}
