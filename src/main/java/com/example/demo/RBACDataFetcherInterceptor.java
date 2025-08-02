package com.example.demo;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import org.springframework.stereotype.Component;

@Component
public class RBACDataFetcherInterceptor implements DataFetcher<Object> {


    @Override
    public Object get(DataFetchingEnvironment environment) throws Exception {
        return null;
    }
}
