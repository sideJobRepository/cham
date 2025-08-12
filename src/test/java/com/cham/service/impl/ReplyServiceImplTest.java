package com.cham.service.impl;

import com.cham.RepositoryAndServiceTestSupport;
import com.cham.controller.request.ReplyCreateRequest;
import com.cham.controller.request.ReplyModifyRequest;
import com.cham.service.ChamMonimapReplyService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

class ReplyServiceImplTest extends RepositoryAndServiceTestSupport {
    
    
    @Autowired
    private ChamMonimapReplyService replyService;
    
    
    @DisplayName("")
    @Test
    void test() throws IOException {
        //given
        
//        File file1 = new File("src/test/java/com/cham/이미지1.png");
//        File file2 = new File("src/test/java/com/cham/이미지2.png");
//
//        MockMultipartFile mockFile1 = new MockMultipartFile(
//                "file", file1.getName(), "image/png", new FileInputStream(file1)
//        );
//
//        MockMultipartFile mockFile2 = new MockMultipartFile(
//                "file", file2.getName(), "image/png", new FileInputStream(file2)
//        );
        //ReplyCreateRequest replyCreateRequest = new ReplyCreateRequest(5L, 1L, "테스트댓글2",List.of(mockFile1, mockFile2));
        ReplyCreateRequest replyCreateRequest = new ReplyCreateRequest(5L, 1L, "테스트댓글2");
        
        
        replyService.insertReply(replyCreateRequest);
    }
    
    @DisplayName("")
    @Test
    void test2() throws IOException {
        Long replyId = 6L;
        String newContent = "수정된 댓글 내용dddd";
        
        // 삭제할 이미지 URL
        String deleteUrl = "https://cham-file.s3.ap-northeast-2.amazonaws.com/46c0e222-bdbc-44e2-9000-22756419e676.png";
        // 대체할 기존 이미지 URL (삭제 후 새로 업로드될 예정)
        String createOldUrl = "https://cham-file.s3.ap-northeast-2.amazonaws.com/062f4a71-26d6-479f-956c-10f49fccc118.png";
        
        // 실제로 존재하는 테스트용 이미지 파일
        File file = new File("src/test/java/com/cham/이미지2.png");
        MockMultipartFile newFile = new MockMultipartFile(
                "file", file.getName(), Files.probeContentType(file.toPath()), Files.readAllBytes(file.toPath())
        );
        
        // 이미지 상태 정의
        List<ReplyModifyRequest.ReplyImageUpdateDto> images = new ArrayList<>();
        images.add(new ReplyModifyRequest.ReplyImageUpdateDto("delete", deleteUrl, null));
        images.add(new ReplyModifyRequest.ReplyImageUpdateDto("create", createOldUrl, newFile));
        
        // 요청 객체 생성
        ReplyModifyRequest request = new ReplyModifyRequest();
        request.setReplyId(replyId);
        request.setReplyCont(newContent);
        request.setImages(images);
        
        // when
        replyService.updateReply(request);
    }
    
    @DisplayName("")
    @Test
    void test3(){
        
        replyService.deleteReply(7L);
    }
    
}