package com.roofandwall.interior.storage;

import jakarta.annotation.PreDestroy;
import java.net.URI;
import java.time.Duration;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.*;
import software.amazon.awssdk.core.checksums.RequestChecksumCalculation;
import software.amazon.awssdk.core.checksums.ResponseChecksumValidation;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.*;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

@Service
public class R2ImageStorage implements ImageStorage {

    private final R2Properties properties;
    private S3Client client;
    private S3Presigner presigner;

    public R2ImageStorage(R2Properties properties) {
        this.properties = properties;
    }

    public boolean configured() {
        return properties.configured();
    }

    private synchronized void initialize() {
        if (client != null) return;
        if (!configured()) throw new ImageStorageException(
            "이미지 저장소 설정이 필요합니다. 관리자에게 문의해 주세요."
        );
        var endpoint = properties.endpointUri();
        var credentials = StaticCredentialsProvider.create(
            AwsBasicCredentials.create(properties.getAccessKeyId(), properties.getSecretAccessKey())
        );
        var config = S3Configuration.builder().pathStyleAccessEnabled(true).chunkedEncodingEnabled(false).build();
        client = S3Client.builder()
            .endpointOverride(endpoint)
            .region(Region.of("auto"))
            .credentialsProvider(credentials)
            .serviceConfiguration(config)
            .requestChecksumCalculation(RequestChecksumCalculation.WHEN_REQUIRED)
            .responseChecksumValidation(ResponseChecksumValidation.WHEN_REQUIRED)
            .httpClientBuilder(
                UrlConnectionHttpClient.builder()
                    .connectionTimeout(Duration.ofSeconds(5))
                    .socketTimeout(Duration.ofSeconds(20))
            )
            .overrideConfiguration(c ->
                c.apiCallTimeout(Duration.ofSeconds(45)).apiCallAttemptTimeout(Duration.ofSeconds(25))
            )
            .build();
        presigner = S3Presigner.builder()
            .endpointOverride(endpoint)
            .region(Region.of("auto"))
            .credentialsProvider(credentials)
            .serviceConfiguration(config)
            .build();
    }

    public void upload(String key, ImageUpload.Image image) {
        initialize();
        try {
            client.putObject(
                PutObjectRequest.builder()
                    .bucket(properties.getBucket())
                    .key(key)
                    .contentType(image.contentType())
                    .cacheControl("private, max-age=300")
                    .build(),
                RequestBody.fromBytes(image.bytes())
            );
        } catch (RuntimeException e) {
            throw new ImageStorageException("이미지 업로드에 실패했습니다. 잠시 후 다시 저장해 주세요.");
        }
    }

    public void delete(String key) {
        initialize();
        try {
            client.deleteObject(DeleteObjectRequest.builder().bucket(properties.getBucket()).key(key).build());
        } catch (RuntimeException e) {
            throw new ImageStorageException("이미지 삭제에 실패했습니다.");
        }
    }

    public URI readUrl(String key) {
        initialize();
        return URI.create(
            presigner
                .presignGetObject(
                    GetObjectPresignRequest.builder()
                        .signatureDuration(Duration.ofMinutes(5))
                        .getObjectRequest(
                            GetObjectRequest.builder()
                                .bucket(properties.getBucket())
                                .key(key)
                                .responseContentDisposition("inline")
                                .build()
                        )
                        .build()
                )
                .url()
                .toString()
        );
    }

    @PreDestroy
    public synchronized void close() {
        if (client != null) client.close();
        if (presigner != null) presigner.close();
    }
}
