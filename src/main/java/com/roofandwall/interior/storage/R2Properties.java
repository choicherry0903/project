package com.roofandwall.interior.storage;

import java.net.URI;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.storage")
public class R2Properties {

    private String endpoint = "";
    private String bucket = "";
    private String accessKeyId = "";
    private String secretAccessKey = "";

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String value) {
        endpoint = value;
    }

    public String getBucket() {
        return bucket;
    }

    public void setBucket(String value) {
        bucket = value;
    }

    public String getAccessKeyId() {
        return accessKeyId;
    }

    public void setAccessKeyId(String value) {
        accessKeyId = value;
    }

    public String getSecretAccessKey() {
        return secretAccessKey;
    }

    public void setSecretAccessKey(String value) {
        secretAccessKey = value;
    }

    public boolean configured() {
        return !endpoint.isBlank() && !bucket.isBlank() && !accessKeyId.isBlank() && !secretAccessKey.isBlank();
    }

    public URI endpointUri() {
        URI uri = URI.create(endpoint);
        if (
            !"https".equals(uri.getScheme()) ||
            uri.getHost() == null ||
            !uri.getHost().endsWith(".r2.cloudflarestorage.com") ||
            uri.getUserInfo() != null ||
            uri.getQuery() != null ||
            uri.getFragment() != null ||
            !(uri.getPath().isEmpty() || uri.getPath().equals("/"))
        ) {
            throw new IllegalArgumentException("R2_ENDPOINT에는 버킷 경로를 제외한 HTTPS S3 API 주소를 설정해 주세요.");
        }
        return uri;
    }
}
