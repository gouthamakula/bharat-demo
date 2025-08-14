package com.example.demo;


import com.example.demo.offlinequery.OfflineQuery;
import com.example.demo.offlinequery.OfflineQueryRepository;
import com.example.demo.offlinequery.storage.S3StorageService;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.context.ApplicationContext;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;

import org.springframework.data.annotation.Id;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.io.IOException;
import java.io.Serializable;
import java.time.Instant;
import java.util.*;
import java.util.function.Consumer;

@Component("accessValidator")
@Slf4j
class accessValidator {
    public Boolean validationService( List<String> vaIds, String saId) {
        List<CustomUserDetails.SAVAAnchorList> savaAnchors = ((CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getSavaAnchorList();
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        userDetails.getSavaAnchorList().add(new CustomUserDetails.SAVAAnchorList("23", Arrays.asList("1", "2", "3")));
        Consumer<String> addValidationMessage = msg ->
                userDetails.getTestVaIds().computeIfAbsent ("devices", k -> new ArrayList<>()).add(msg);
        addValidationMessage.accept("testVaIds");
        Authentication auth = new UsernamePasswordAuthenticationToken(userDetails, null, null);
        SecurityContextHolder.getContext().setAuthentication(auth);
        return true;
//        return savaAnchors.stream().anyMatch(savaAnchor -> savaAnchor.getSaId().equals(saId) && new HashSet<>(savaAnchor.getVaIds()).containsAll(vaIds));
    }
}

@Controller
public class BookController {

    @AllArgsConstructor
    @NoArgsConstructor
    public static class WorkingField implements Serializable {
        String uuid;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Book implements Serializable {
        public String title;
        public String requestId;
        public Integer author;
        public WorkingField field;
    }

    @Autowired
    private OfflineQueryRepository repository;
    @Autowired
    private S3StorageService s3StorageService;


    @QueryMapping
    @PreAuthorize("@accessValidator.validationService(#vaIds, #saId)")
    public Book getBooks(@Argument List<String> vaIds, @Argument String saId, @Argument Boolean offlineMode) throws IOException {
        if (offlineMode) {
            String requestId = processOfflineQuery("testQuery" + saId);
            Book book = Book.builder().requestId(requestId).title(
                    "saId: " + saId + " vaIds: " + vaIds
            ).build();
            s3StorageService.uplaodZippedObject(requestId, book);
            return book;
        }
        return Book.builder().title("title1").author(1).field(new WorkingField(UUID.randomUUID().toString())).build();
    }

    public String processOfflineQuery(String graphqlQuery) {
        String requestId = java.util.UUID.randomUUID().toString();

        OfflineQuery query = OfflineQuery.builder()
                .requestId(requestId)
                .queryText(graphqlQuery)
                .timestamp(java.time.Instant.now())
                .status("PENDING")
                .build();

        this.repository.save(query);
        return requestId;
    }


}
