package com.cham.config;

import com.cham.file.UploadResult;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Component
@RequiredArgsConstructor
public class S3FileUtils {
    private final S3Client s3Client;
    
    @Value("${spring.cloud.aws.s3.bucket}")
    private String bucketName;
    
    public List<UploadResult> storeFiles(List<MultipartFile> multipartFiles) {
        List<UploadResult> uploadFiles = new ArrayList<>();
        for (MultipartFile multipartFile : multipartFiles) {
            if (!multipartFile.isEmpty()) {
                uploadFiles.add(storeFile(multipartFile));
            }
        }
        return uploadFiles;
    }
    
    
    public UploadResult storeFile(MultipartFile multipartFile) {
        
        if (multipartFile.isEmpty()) return null;
        
        String originalFilename = multipartFile.getOriginalFilename();
        String uuid = UUID.randomUUID().toString();
        String ext = extractExt(originalFilename);
        String storeFileName =  uuid + "." + ext;
        
        try {
            String encodedFilename = URLEncoder.encode(originalFilename, StandardCharsets.UTF_8);
            Map<String, String> metadata = new HashMap<>();
            metadata.put("Content-Type", multipartFile.getContentType());
            metadata.put("Content-Disposition", "inline");
            metadata.put("original-filename", encodedFilename); //원래 파일명 메타데이터에 저장
            
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(storeFileName)
                    .metadata(metadata)
                    .build();
            
            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(multipartFile.getBytes()));
            
            String url = s3Client.utilities()
                    .getUrl(b -> b.bucket(bucketName).key(storeFileName))
                    .toExternalForm();
            
            return new UploadResult(url, uuid,originalFilename);
            
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    public void deleteFile(String fileUrl) {
        String key = getFileNameFromUrl(fileUrl);
        DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();
        s3Client.deleteObject(deleteObjectRequest);
    }
    
    private String getFileNameFromUrl(String fileUrl) {
        try {
            URL url = new URL(fileUrl);
            String path = url.getPath(); // e.g. /images/filename.jpg
            return path.startsWith("/") ? path.substring(1) : path;
        } catch (MalformedURLException e) {
            throw new RuntimeException("Invalid S3 file URL", e);
        }
    }
    
    private String createStoreFileName(String originalFilename) {
        String ext = extractExt(originalFilename);
        String uuid = UUID.randomUUID().toString();
        return uuid + "." + ext;
    }
    
    private String extractExt(String originalFilename) {
        int pos = originalFilename.lastIndexOf(".");
        return originalFilename.substring(pos + 1);
    }
    
}
