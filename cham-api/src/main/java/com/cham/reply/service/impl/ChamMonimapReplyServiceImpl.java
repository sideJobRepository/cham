package com.cham.reply.service.impl;

import com.cham.config.S3FileUtils;
import com.cham.dto.request.ReplyCreateRequest;
import com.cham.dto.request.ReplyModifyRequest;
import com.cham.dto.response.ApiResponse;
import com.cham.file.UploadResult;
import com.cham.reply.entity.ChamMonimapReply;
import com.cham.replyimage.entity.ChamMonimapReplyImage;
import com.cham.reply.service.ChamMonimapReplyService;
import com.cham.replyimage.repository.ChamMonimapReplyImageRepository;
import com.cham.reply.repository.ChamMonimapReplyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
@Transactional
public class ChamMonimapReplyServiceImpl implements ChamMonimapReplyService {
    
    private final ChamMonimapReplyRepository replyRepository;
    private final ChamMonimapReplyImageRepository replyImageRepository;
    private final S3FileUtils s3FileUtils;
    
    @Override
    public ApiResponse insertReply(ReplyCreateRequest request) {
        ChamMonimapReply reply = new ChamMonimapReply(request.getMemberId(), request.getCardUseAddrId(), request.getReplyCont());
        ChamMonimapReply saveReply = replyRepository.save(reply);
        List<UploadResult> list = s3FileUtils.storeFiles(request.getFileList());
        if(!list.isEmpty()){
            for (UploadResult image : list) {
                replyImageRepository.save(new ChamMonimapReplyImage(saveReply, image.getUrl()));
            }
        }
        return new ApiResponse(200,true,"댓글이 작성 되었습니다.");
    }
    
    @Override
    public ApiResponse updateReply(ReplyModifyRequest request) {
        Long replyId = request.getReplyId();
        ChamMonimapReply reply = replyRepository.findById(replyId).orElseThrow(() -> new RuntimeException("존재하지 않는 댓글입니다."));
        reply.modifyReply(request);
        request.getImages()
                .stream().filter(img -> "delete".equals(img.getState()))
                .forEach(item -> {
                    String imgUrl = item.getImgUrl();
                    s3FileUtils.deleteFile(imgUrl);
                    replyImageRepository.deleteByImageUrl(imgUrl);
                });
        
        request.getImages()
                .stream().filter(img -> "create".equals(img.getState()))
                .forEach(item -> {
                    replyImageRepository.save(new ChamMonimapReplyImage(reply, s3FileUtils.storeFile(item.getFile()).getUrl()));
                });
        return new ApiResponse(200,true,"댓글이 수정 되었습니다.");
    }
    
    @Override
    public ApiResponse deleteReply(Long replyId) {
        ChamMonimapReply reply = replyRepository.findById(replyId).orElseThrow(() -> new RuntimeException("존재하지 않는 댓글입니다."));
        List<String> replyImageUrls= replyImageRepository.findByReplyImageUrlInReplyId(reply.getChamMonimapReplyId());
        if(!replyImageUrls.isEmpty()){
            replyImageUrls.forEach(s3FileUtils::deleteFile);
            replyImageRepository.deleteByReplyImage(reply.getChamMonimapReplyId());
        }
        replyRepository.delete(reply);
        return new ApiResponse(200,true,"댓글이 삭제 되었습니다.");
    }
}
