package com.cham.service.impl;

import com.cham.config.S3FileUtils;
import com.cham.controller.request.ReplyCreateRequest;
import com.cham.controller.request.ReplyModifyRequest;
import com.cham.controller.response.ApiResponse;
import com.cham.entity.Reply;
import com.cham.entity.ReplyImage;
import com.cham.repository.ReplyImageRepository;
import com.cham.repository.ReplyRepository;
import com.cham.service.ReplyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
@Transactional
public class ReplyServiceImpl implements ReplyService {
    
    private final ReplyRepository replyRepository;
    private final ReplyImageRepository replyImageRepository;
    private final S3FileUtils s3FileUtils;
    
    @Override
    public ApiResponse insertReply(ReplyCreateRequest request) {
        Reply reply = new Reply(request.getMemberId(), request.getCardUseAddrId(), request.getReplyCont());
        Reply saveReply = replyRepository.save(reply);
        List<String> list = s3FileUtils.storeFiles(request.getFileList());
        if(!list.isEmpty()){
            for (String image : list) {
                replyImageRepository.save(new ReplyImage(saveReply, image));
            }
        }
        return new ApiResponse(200,true,"댓글이 작성 되었습니다.");
    }
    
    @Override
    public ApiResponse updateReply(ReplyModifyRequest request) {
        Long replyId = request.getReplyId();
        Reply reply = replyRepository.findById(replyId).orElseThrow(() -> new RuntimeException("존재하지 않는 댓글입니다."));
        reply.modifyReply(request);
        replyRepository.flush();; // 왜 변경감지가 안되지?
        request.getImages()
                .stream().filter(img -> "delete".equals(img.getState()))
                .forEach(item -> {
                    String imgUrl = item.getImgUrl();
                    s3FileUtils.deleteFile(imgUrl);
                    replyImageRepository.deletebyImageUrl(imgUrl);
                });
        
        request.getImages()
                .stream().filter(img -> "create".equals(img.getState()))
                .forEach(item -> {
                    replyImageRepository.save(new ReplyImage(reply, s3FileUtils.storeFile(item.getFile())));
                });
        return new ApiResponse(200,true,"댓글이 수정 되었습니다.");
    }
    
    @Override
    public ApiResponse deleteReply(Long replyId) {
        Reply reply = replyRepository.findById(replyId).orElseThrow(() -> new RuntimeException("존재하지 않는 댓글입니다."));
        List<String> replyImageUrls= replyImageRepository.findByReplyImageUrlInReplyId(reply.getReplyId());
        if(!replyImageUrls.isEmpty()){
            replyImageUrls.forEach(s3FileUtils::deleteFile);
            replyImageRepository.deletebyReplyImage(reply.getReplyId());
        }
        replyRepository.delete(reply);
        return new ApiResponse(200,true,"댓글이 삭제 되었습니다.");
    }
}
