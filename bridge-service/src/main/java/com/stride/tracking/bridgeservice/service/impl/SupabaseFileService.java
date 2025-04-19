package com.stride.tracking.bridgeservice.service.impl;

import com.stride.tracking.commons.exception.StrideException;
import com.stride.tracking.dto.response.FileLinkResponse;
import com.stride.tracking.bridgeservice.client.SupabaseFileUploader;
import com.stride.tracking.bridgeservice.constant.Message;
import com.stride.tracking.bridgeservice.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SupabaseFileService implements FileService {
    private final SupabaseFileUploader supabaseFileUploader;

    @Override
    public FileLinkResponse upload(MultipartFile file) {
        try {
            String fileName = generateFileName(Objects.requireNonNull(file.getOriginalFilename()));

            String fileUrl = supabaseFileUploader.uploadFileToSupabase(file, fileName);

            return FileLinkResponse.builder().file(fileUrl).build();
        } catch (Exception e) {
            throw new StrideException(HttpStatus.INTERNAL_SERVER_ERROR, Message.FILE_UPLOAD_FAIL);
        }
    }

    private String generateFileName(String originalFilename) {
        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        return UUID.randomUUID() + extension;
    }


}
