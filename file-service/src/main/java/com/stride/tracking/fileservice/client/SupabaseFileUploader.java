package com.stride.tracking.fileservice.client;

import com.stride.tracking.commons.exception.StrideException;
import com.stride.tracking.fileservice.constant.Message;
import lombok.RequiredArgsConstructor;
import lombok.experimental.NonFinal;
import lombok.extern.log4j.Log4j2;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Objects;

@RequiredArgsConstructor
@Component
@Log4j2
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

    public String uploadFileToSupabase(MultipartFile file, String fileName) throws IOException {
        String url = SUPABASE_URL + "/storage/v1/object/" + SUPABASE_BUCKET + "/" + fileName;
        log.info("[uploadFileToSupabase] Start uploading file: {} to {}", fileName, url);

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", fileName,
                        RequestBody.create(file.getBytes(), MediaType.parse(Objects.requireNonNull(file.getContentType()))))
                .build();

        Request request = new Request.Builder()
                .url(url)
                .header("Authorization", SUPABASE_API_KEY)
                .header("Content-Type", "application/octet-stream")
                .post(requestBody)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.code() == ENTITY_TOO_LARGE_CODE) {
                log.error("[uploadFileToSupabase] Upload failed - file too large: {}", fileName);
                throw new StrideException(HttpStatus.PAYLOAD_TOO_LARGE, Message.FILE_UPLOAD_TOO_LARGE);
            } else if (!response.isSuccessful()) {
                log.error("[uploadFileToSupabase] Upload failed - status code: {}, message: {}", response.code(), response.message());
                throw new StrideException(HttpStatus.INTERNAL_SERVER_ERROR, Message.FILE_UPLOAD_TOO_LARGE);
            }

            String publicUrl = SUPABASE_URL + "/storage/v1/object/public/" + SUPABASE_BUCKET + "/" + fileName;
            log.info("[uploadFileToSupabase] Upload successful: {}", publicUrl);

            return publicUrl;
        } catch (IOException e) {
            log.error("[uploadFileToSupabase] IOException while uploading file: {}", e.getMessage(), e);
            throw e;
        }
    }
}
