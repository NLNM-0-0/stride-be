package com.stride.tracking.bridgeservice.client;

import com.stride.tracking.bridgeservice.constant.Message;
import com.stride.tracking.commons.exception.StrideException;
import lombok.RequiredArgsConstructor;
import lombok.experimental.NonFinal;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Objects;

@RequiredArgsConstructor
@Component
public class SupabaseFileUploader {
    @Value("${storage.supabase.url}")
    @NonFinal
    private String SUPABASE_URL;

    @Value("${storage.supabase.bucket}")
    @NonFinal
    private String SUPABASE_BUCKET;

    @Value("${storage.supabase.api-key}")
    @NonFinal
    private String SUPABASE_API_KEY;

    private final OkHttpClient client;

    private static final int ENTITY_TOO_LARGE_CODE = 413;

    public String uploadFileToSupabase(byte[] data, String fileName, String contentType) throws IOException {
        RequestBody requestBody = RequestBody.create(data, MediaType.parse(contentType));
        Request request = buildRequest(fileName, requestBody);
        return executeAndReturnUrl(fileName, request);
    }

    public String uploadFileToSupabase(MultipartFile file, String fileName) throws IOException {
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", fileName,
                        RequestBody.create(file.getBytes(), MediaType.parse(Objects.requireNonNull(file.getContentType()))))
                .build();
        Request request = buildRequest(fileName, requestBody);
        return executeAndReturnUrl(fileName, request);
    }

    private Request buildRequest(String fileName, RequestBody requestBody) {
        String url = SUPABASE_URL + "/storage/v1/object/" + SUPABASE_BUCKET + "/" + fileName;

        return new Request.Builder()
                .url(url)
                .header("Authorization", SUPABASE_API_KEY)
                .header("Content-Type", "application/octet-stream")
                .post(requestBody)
                .build();
    }

    private String executeAndReturnUrl(String fileName, Request request) throws IOException {
        try (Response response = client.newCall(request).execute()) {
            if (response.code() == ENTITY_TOO_LARGE_CODE) {
                throw new StrideException(HttpStatus.PAYLOAD_TOO_LARGE, Message.FILE_UPLOAD_TOO_LARGE);
            } else if (!response.isSuccessful()) {
                throw new StrideException(HttpStatus.INTERNAL_SERVER_ERROR, Message.FILE_UPLOAD_FAIL);
            }

            return SUPABASE_URL + "/storage/v1/object/public/" + SUPABASE_BUCKET + "/" + fileName;
        }
    }
}
