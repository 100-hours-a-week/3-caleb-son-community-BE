package com.ktb.community.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.IOException;
import java.util.UUID;

/**
 * 완전한 AWS S3 업로드 구현
 * 실제 사용 시에는 이 클래스로 S3Service를 교체하세요.
 */
@Service
public class S3ServiceComplete {
    
    private final S3Client s3Client;
    private final String bucketName;
    private final String region;
    
    public S3ServiceComplete(@Value("${aws.s3.bucket-name}") String bucketName,
                             @Value("${aws.s3.access-key}") String accessKey,
                             @Value("${aws.s3.secret-key}") String secretKey,
                             @Value("${aws.s3.region}") String region) {
        this.bucketName = bucketName;
        this.region = region;
        
        // S3 클라이언트 생성
        this.s3Client = S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKey, secretKey)))
                .build();
    }
    
    public String uploadImage(MultipartFile file) throws IOException {
        try {
            // 고유한 파일명 생성
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String fileName = "images/" + UUID.randomUUID().toString() + extension;
            
            // S3에 파일 업로드
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileName)
                    .contentType(file.getContentType())
                    .contentLength(file.getSize())
                    .build();
            
            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(
                    file.getInputStream(), file.getSize()));
            
            // S3 URL 반환
            return String.format("https://%s.s3.%s.amazonaws.com/%s", bucketName, region, fileName);
            
        } catch (S3Exception e) {
            throw new IOException("S3 업로드 실패: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new IOException("파일 업로드 중 오류 발생: " + e.getMessage(), e);
        }
    }
}
