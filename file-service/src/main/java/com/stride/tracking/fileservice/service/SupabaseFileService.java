package com.stride.tracking.fileservice.service;

import com.stride.tracking.commons.exception.StrideException;
import com.stride.tracking.dto.response.FileLinkResponse;
import com.stride.tracking.fileservice.constant.Message;
import com.stride.tracking.fileservice.client.SupabaseFileUploader;
import com.stride.tracking.fileservice.service.impl.FileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Log4j2
public class SupabaseFileService implements FileService {
    private final SupabaseFileUploader supabaseFileUploader;

    @Override
    public FileLinkResponse upload(MultipartFile file) {
        try {
            log.info("[upload] Start uploading file: originalName={}", file.getOriginalFilename());

            String fileName = generateFileName(Objects.requireNonNull(file.getOriginalFilename()));
            log.debug("[upload] Generated filename: {}", fileName);

            String fileUrl = supabaseFileUploader.uploadFileToSupabase(file, fileName);
            log.info("[upload] Successfully uploaded file. URL: {}", fileUrl);

            return FileLinkResponse.builder().file(fileUrl).build();
        } catch (Exception e) {
            throw new StrideException(HttpStatus.INTERNAL_SERVER_ERROR, Message.FILE_UPLOAD_FAIL);
        }
    }

    private String generateFileName(String originalFilename) {
        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String generatedName = UUID.randomUUID() + extension;
        log.debug("[generateFileName] Generated new file name: {}", generatedName);
        return generatedName;
    }

}
