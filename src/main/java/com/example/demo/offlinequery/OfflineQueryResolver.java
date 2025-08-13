package com.example.demo.offlinequery;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.Argument;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class OfflineQueryResolver {

    private final OfflineQueryRepository repository;

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
    ) {
        OfflineQuery query = repository.findByRequestId(requestId)
                .orElseThrow(() -> new RuntimeException("Query not found"));

        OfflineQueryStatus status = new OfflineQueryStatus();
        status.setRequestId(query.getRequestId());
        status.setStatus(query.getStatus());
        status.setS3Link("");

        if ("COMPLETED".equals(query.getStatus()) && Boolean.TRUE.equals(getData)) {
            status.setResults(null);
        }

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
