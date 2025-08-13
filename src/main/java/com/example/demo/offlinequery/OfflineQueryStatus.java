package com.example.demo.offlinequery;

import lombok.Data;

import java.util.Map;

@Data
public class OfflineQueryStatus {
    private String requestId;
    private String status;
    private String s3Link;
    private Map<String, Object> results;
}
