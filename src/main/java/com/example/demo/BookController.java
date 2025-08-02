package com.example.demo;


import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
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

    @Autowired
    ApplicationContext applicationContext;

    @AllArgsConstructor
    static class WorkingField {
        String uuid;
    }

    @Autowired
    CacheManager cacheManager;

    @Data
    @Builder
    static class Book {
        public String title;
        public Integer author;
        @JsonIgnore(value=false)
        public WorkingField field;
    }

    @QueryMapping
    @PreAuthorize("@accessValidator.validationService(#vaIds, #saId)")
    public Book getBooks(@Argument List<String> vaIds, @Argument String saId) {
        SecurityContextHolder.getContext();
        System.out.println(applicationContext.getBeanDefinitionNames());
        return Book.builder().title("title1").author(1).field(new WorkingField(UUID.randomUUID().toString())).build();
    }

}
