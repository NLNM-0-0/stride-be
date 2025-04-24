package com.stride.tracking.bridgeservice.service;

import com.stride.tracking.dto.file.response.FileLinkResponse;
import org.springframework.web.multipart.MultipartFile;

public interface FileService {
    FileLinkResponse upload(byte[] data, String originalFileName, String contentType);
    FileLinkResponse upload(MultipartFile file);
}
