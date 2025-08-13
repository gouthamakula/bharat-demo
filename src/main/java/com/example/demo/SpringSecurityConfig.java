package com.example.demo;

import graphql.ExecutionResult;
import graphql.execution.*;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLFieldDefinition;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.graphql.GraphQlSourceBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.graphql.data.method.annotation.support.AnnotatedControllerConfigurer;
import org.springframework.graphql.execution.DataFetcherExceptionResolver;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.spec.PSource;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true)
public class SpringSecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, AuthUserHeaderFilter authFilter) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests( authorizeConfig -> {
                    authorizeConfig.anyRequest().permitAll();
                })
                .addFilterBefore(authFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    public static class CustomAsyncExecutionStrategy extends AsyncExecutionStrategy {
        public CustomAsyncExecutionStrategy(DataFetcherExceptionHandler exceptionHandler) {
            super(exceptionHandler);
        }

        protected FetchedValue unboxPossibleDataFetcherResult(ExecutionContext executionContext,
                                                              ExecutionStrategyParameters parameters,
                                                              Object result) {
            if (result instanceof BookController.Book) {
                AccessFieldNullifier.nullifyAccessAnnotatedFields(result);
//                return super.unboxPossibleDataFetcherResult(executionContext, parameters, (Object) BookController.Book.builder().title("test working").build());
            }
            return super.unboxPossibleDataFetcherResult(executionContext, parameters, result);
        }


        protected BiConsumer<List<ExecutionResult>, Throwable> handleResults(ExecutionContext executionContext, List<String> fieldNames, CompletableFuture<ExecutionResult> overallResult) {
            return super.handleResults(executionContext, fieldNames, overallResult);
        }
    }


    @Component
    public static class MyDataFetcher implements DataFetcher<String> {

        @Override
        public String get(DataFetchingEnvironment environment) throws Exception {
            // Original logic:
            // String input = environment.getArgument("someInput");
            // return "Hello, " + input + "!";

            // Modified logic to change the response:
            String modifiedInput = environment.getArgument("someInput");
            if (modifiedInput != null && modifiedInput.equalsIgnoreCase("world")) {
                return "Greetings, Planet Earth!";
            } else {
                return "Hello, " + modifiedInput + " from the modified DataFetcher!";
            }
        }
    }

    @Bean
    public AnnotatedControllerConfigurer annotatedControllerConfigurer() {
        AnnotatedControllerConfigurer configurer = new AnnotatedControllerConfigurer();

        // Add custom validator


        return configurer;
    }

    @Bean
    GraphQlSourceBuilderCustomizer sourceBuilderCustomizer(
            ObjectProvider<DataFetcherExceptionResolver> resolvers) {

                DataFetcherExceptionHandler exceptionHandler =
                DataFetcherExceptionResolver.createExceptionHandler(resolvers.stream().toList());

        AsyncExecutionStrategy strategy = new CustomAsyncExecutionStrategy(exceptionHandler);

        return sourceBuilder -> {
            sourceBuilder.configureGraphQl(builder ->
                    builder.queryExecutionStrategy(strategy).mutationExecutionStrategy(strategy)); };
    }
}

@AllArgsConstructor
@Getter
@Builder
class CustomUserDetails implements UserDetails {

    @Data
    @Builder
    static class SAVAAnchorList {
        private String saId;
        private List<String> vaIds;
    }
    private Map<String, List<String>> testVaIds;

    private String username;
    private List<SAVAAnchorList> savaAnchorList;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}

@Component
class AuthUserHeaderFilter extends OncePerRequestFilter {
    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
//        String ccoid = request.getHeader("AUTH_USER");
//        if (ccoid == null) ccoid = "1";
//        String url = "http://localhost:8080/user/content";
//        try {
//            UserContextResponse resp = restTemplate.getForObject(url, UserContextResponse.class);
//        } catch (Exception e) {
//            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "SAVA validation failed");
//        }
        List<UserContextResponse> resps = Arrays.asList(new UserContextResponse("123", Arrays.asList("232", "2424")),
                new UserContextResponse("245",  Arrays.asList("828", "825")));
        List<CustomUserDetails.SAVAAnchorList> savaAnchorList = resps.stream().map(resp -> new CustomUserDetails.SAVAAnchorList.SAVAAnchorListBuilder().saId(resp.getSaId()).vaIds(resp.getVaIds()).build()).collect(Collectors.toList());
        CustomUserDetails userDetails = CustomUserDetails.builder().savaAnchorList(savaAnchorList).testVaIds(new HashMap<>()).build();
        Authentication auth = new UsernamePasswordAuthenticationToken(userDetails, null, null);
        SecurityContextHolder.getContext().setAuthentication(auth);

        filterChain.doFilter(request, response);
    }
}

@AllArgsConstructor
@Getter
class UserContextResponse {
    private String saId;
    private List<String> vaIds;
}
