package com.example.demo.offlinequery;


import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import javax.swing.text.html.Option;
import java.util.Optional;

@Repository
public interface OfflineQueryRepository extends MongoRepository<OfflineQuery, String> {
    Optional<OfflineQuery> findByRequestId(String requestId);
}
