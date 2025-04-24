package com.stride.tracking.bridgeservice.controller;

import com.stride.tracking.bridgeservice.service.FileService;
import com.stride.tracking.dto.file.response.FileLinkResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/files")
@RequiredArgsConstructor
public class FileController {
    private final FileService fileService;

    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<FileLinkResponse> upload(
            @RequestParam("file") MultipartFile multipartFile) {
        return new ResponseEntity<>(fileService.upload(multipartFile), HttpStatus.OK);
    }

    @PostMapping(
            path = "/raw",
            consumes = MediaType.APPLICATION_OCTET_STREAM_VALUE
    )
    public ResponseEntity<FileLinkResponse> uploadRaw(
            @RequestBody byte[] data,
            @RequestParam("fileName") String fileName,
            @RequestParam(value = "contentType", defaultValue = "image/png") String contentType) {
        return new ResponseEntity<>(fileService.upload(data, fileName, contentType), HttpStatus.OK);
    }
}
