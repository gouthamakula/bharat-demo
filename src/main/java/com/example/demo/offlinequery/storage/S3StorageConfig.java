package com.example.demo.offlinequery.storage;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.endpoints.S3EndpointProvider;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.net.URI;

@Configuration
public class S3StorageConfig {

    @Bean
    public S3Client s3Client() {
       return S3Client.builder()
               .region(Region.AP_SOUTH_1)
               .credentialsProvider(
                       StaticCredentialsProvider.create(
                               AwsBasicCredentials.create("test", "test")
                       )
               )
               .serviceConfiguration(S3Configuration.builder()
                       .pathStyleAccessEnabled(true)
                       .build())
               .endpointOverride(URI.create("http://localhost:4566"))
               .build();
    }

    @Bean
    public S3Presigner s3Presigner(S3Client s3Client) {
        return S3Presigner.builder()
                .region(Region.AP_SOUTH_1)
                .serviceConfiguration(S3Configuration.builder()
                        .pathStyleAccessEnabled(true)
                        .build())
                .credentialsProvider(
                        StaticCredentialsProvider.create(
                                AwsBasicCredentials.create("test", "test")
                        )
                )
                .endpointOverride(URI.create("http://localhost:4566"))
                .build();
    }


}
