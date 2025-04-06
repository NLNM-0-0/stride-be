package com.stride.tracking.fileservice.service.impl;

import com.stride.tracking.dto.response.FileLinkResponse;
import org.springframework.web.multipart.MultipartFile;

public interface FileService {
    FileLinkResponse upload(MultipartFile file);
}
