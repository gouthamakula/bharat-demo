package com.example.demo.offlinequery;

import org.springframework.stereotype.Service;

@Service
public class S3Service {

    public String uploadResults(String requestId, Object data) {
        // Mock S3 URL
        return "https://mock-s3.local/offline-queries/" + requestId + ".json";
    }
}

