package com.example.demo.offlinequery.storage;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectAclRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Duration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

@Service
public class S3StorageService {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    @Value("${app.s3.bucket:udl-graphql-offline}")
    private String bucketName;

    @Autowired
    public S3StorageService(S3Client s3Client,  S3Presigner s3Presigner) {
        this.s3Client = s3Client;
        this.s3Presigner = s3Presigner;
    }

    public void uplaodZippedObject(String key, Object data) throws IOException {
        byte[] jsonBytes = new ObjectMapper().writeValueAsBytes(data);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ZipOutputStream zipOutputStream = new ZipOutputStream(byteArrayOutputStream);
        zipOutputStream.putNextEntry(new ZipEntry("data.json"));
        zipOutputStream.write(jsonBytes);
        zipOutputStream.closeEntry();
        zipOutputStream.close();

        s3Client.putObject(
                PutObjectRequest.builder()
                        .bucket(bucketName)
                        .key(key + ".zip")
                        .build(),
                RequestBody.fromBytes(byteArrayOutputStream.toByteArray())
        );
    }

    public <T> T downloadZippedObject(String key, Class<T> clazz) throws IOException {
        ResponseBytes<GetObjectResponse> s3Object = s3Client.getObjectAsBytes(
                GetObjectRequest.builder()
                        .bucket(bucketName)
                        .key(key + ".zip")
                        .build()
        );
        try(ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(s3Object.asByteArray());
            ZipInputStream zipInputStream = new ZipInputStream(byteArrayInputStream)) {
               zipInputStream.getNextEntry();
               return new ObjectMapper().readValue(zipInputStream, clazz);
            }
    }

    public String generatePresignedUrl(String key) {

        PresignedGetObjectRequest presignedGetObjectRequest = s3Presigner.presignGetObject(
                GetObjectPresignRequest.builder()
                        .signatureDuration(Duration.ofMinutes(30))
                        .getObjectRequest(GetObjectRequest.builder()
                                .bucket(bucketName)
                                .key(key + ".zip")
                                .build())
                        .build()
        );

        return presignedGetObjectRequest.url().toString();
    }

}
