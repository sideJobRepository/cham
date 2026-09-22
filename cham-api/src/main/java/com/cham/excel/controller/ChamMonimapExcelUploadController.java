package com.cham.excel.controller;


import com.cham.dto.response.ApiResponse;
import com.cham.caruse.service.ChamMonimapCardUseService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/cham")
@RequiredArgsConstructor
public class ChamMonimapExcelUploadController {

    
    private final ChamMonimapCardUseService cardUseService;
    
    @PostMapping("/upload")
    public ApiResponse uploadExcel(MultipartFile multipartFile) {
        return cardUseService.insertCardUse(multipartFile);
    }
    
    @DeleteMapping("/upload/{deleteKey}")
    public ApiResponse deleteExcel(@PathVariable String deleteKey) {
        return cardUseService.deleteExcel(deleteKey);
    }
}
