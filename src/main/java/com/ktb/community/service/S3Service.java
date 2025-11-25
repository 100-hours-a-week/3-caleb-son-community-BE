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

import java.io.IOException;
import java.util.UUID;

@Service
public class S3Service {
    
    private final String bucketName;
    private final String region;
    private final String accessKey;
    private final String secretKey;
    
    public S3Service(@Value("${aws.s3.bucket-name}") String bucketName,
                     @Value("${aws.s3.region}") String region,
                     @Value("${aws.s3.access-key}") String accessKey,
                     @Value("${aws.s3.secret-key}") String secretKey) {
        this.bucketName = bucketName;
        this.region = region;
        this.accessKey = accessKey;
        this.secretKey = secretKey;
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
            
            // AWS S3 클라이언트 생성
            S3Client s3Client = S3Client.builder()
                    .region(Region.of(region))
                    .credentialsProvider(StaticCredentialsProvider.create(
                            AwsBasicCredentials.create(accessKey, secretKey)))
                    .build();
            
            // S3 업로드 요청 생성
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileName)
                    .contentType(file.getContentType())
                    .contentLength(file.getSize())
                    .build();
            
            // 파일 업로드 실행
            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(
                    file.getInputStream(), file.getSize()));
            
            // S3 URL 반환
            return String.format("https://%s.s3.%s.amazonaws.com/%s", bucketName, region, fileName);
            
        } catch (Exception e) {
            throw new IOException("파일 업로드 중 오류 발생: " + e.getMessage(), e);
        }
    }
    
    public String uploadProfileImage(MultipartFile file) throws IOException {
        try {
            // 프로필 이미지용 고유한 파일명 생성
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String fileName = "profiles/" + UUID.randomUUID().toString() + extension;
            
            // AWS S3 클라이언트 생성
            S3Client s3Client = S3Client.builder()
                    .region(Region.of(region))
                    .credentialsProvider(StaticCredentialsProvider.create(
                            AwsBasicCredentials.create(accessKey, secretKey)))
                    .build();
            
            // S3 업로드 요청 생성
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileName)
                    .contentType(file.getContentType())
                    .contentLength(file.getSize())
                    .build();
            
            // 파일 업로드 실행
            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(
                    file.getInputStream(), file.getSize()));
            
            // S3 URL 반환
            return String.format("https://%s.s3.%s.amazonaws.com/%s", bucketName, region, fileName);
            
        } catch (Exception e) {
            throw new IOException("프로필 이미지 업로드 중 오류 발생: " + e.getMessage(), e);
        }
    }
}
