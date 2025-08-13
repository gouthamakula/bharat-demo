package com.example.demo.offlinequery;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "offline_queries") // name of MongoDB collection
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OfflineQuery {
    @Id
    private String id; // MongoDB will generate if null

    private String requestId;
    private String queryText;
    private Instant timestamp;
    private String status;
}
