package com.cham.controller;


import com.cham.controller.response.ApiResponse;
import com.cham.entity.CardUse;
import com.cham.service.CardUseService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/cham")
@RequiredArgsConstructor
public class ExcelUploadController {

    
    private final CardUseService cardUseService;
    
    @PostMapping("/upload")
    public ApiResponse uploadExcel(MultipartFile multipartFile) {
        return cardUseService.insertCardUse(multipartFile);
    }
    
    @DeleteMapping("/upload/{deleteKey}")
    public ApiResponse deleteExcel(@PathVariable String deleteKey) {
        return cardUseService.deleteExcel(deleteKey);
    }
}
