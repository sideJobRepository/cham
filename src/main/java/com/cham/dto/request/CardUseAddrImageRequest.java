package com.cham.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CardUseAddrImageRequest {
    private Long cardUseAddrId;
    private MultipartFile cardUseImageUrl;
}
