package com.cham.controller.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ReplyModifyRequest {
    
    private Long replyId;
    
    private String replyCont;
    
    private List<ReplyImageUpdateDto> images;
    
    public List<ReplyImageUpdateDto> getImages() {
        if (this.images == null) {
            images = new ArrayList<>();
        }
        return images;
    }
    
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ReplyImageUpdateDto {
        private String state;   // create, delete, normal
        private String imgUrl;
        private MultipartFile file;
    }
}
