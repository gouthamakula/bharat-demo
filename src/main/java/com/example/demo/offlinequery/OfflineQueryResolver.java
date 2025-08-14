package com.example.demo.offlinequery;


import com.example.demo.BookController;
import com.example.demo.offlinequery.storage.S3StorageService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.Argument;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

@Controller
public class OfflineQueryResolver {

    @Autowired
    private OfflineQueryRepository repository;
    @Autowired
    private S3StorageService s3StorageService;

    // 1. Return list of previous queries
    @QueryMapping
    public List<OfflineQuery> listOfflineQueries() {
        return repository.findAll();
    }

    // 2. Get status/results
    @QueryMapping
    public OfflineQueryStatus getOfflineQueryStatus(
            @Argument String requestId,
            @Argument Boolean getData
    ) throws IOException {
        OfflineQuery query = repository.findByRequestId(requestId)
                .orElseThrow(() -> new RuntimeException("Query not found"));

        OfflineQueryStatus status = new OfflineQueryStatus();
        status.setRequestId(query.getRequestId());
        status.setStatus(query.getStatus());
        status.setS3Link("");
        if (getData) {
            status.setResults(
                    new ObjectMapper().convertValue(
                        s3StorageService.downloadZippedObject(requestId, BookController.Book.class),
                        HashMap.class
                    )
            );
        }

        if ("COMPLETED".equals(query.getStatus()) && Boolean.TRUE.equals(getData)) {
            status.setResults(null);
        }
        status.setS3Link(s3StorageService.generatePresignedUrl(requestId));

        return status;
    }

    // 3. Get original query text
    @QueryMapping
    public String getOfflineQueryText(@Argument String requestId) {
        return repository.findByRequestId(requestId)
                .map(OfflineQuery::getQueryText)
                .orElseThrow(() -> new RuntimeException("Query not found"));
    }
}
