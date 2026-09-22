package com.cham.file;

import lombok.Getter;

@Getter
public class UploadResult {
    private final String url;
    private final String uuid;
    private final String originalFilename;
    
    public UploadResult(String url, String uuid,String originalFilename) {
        this.url = url;
        this.uuid = uuid;
        this.originalFilename = originalFilename;
    }
    
}
