package com.stride.tracking.notificationservice.controller;

import com.stride.tracking.commons.response.SimpleResponse;
import com.stride.tracking.dto.request.SendEmailRequest;
import com.stride.tracking.notificationservice.service.MailService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/emails")
public class EmailController {
    private final MailService mailService;

    @PostMapping("/send")
    ResponseEntity<SimpleResponse> sendEmail(@RequestBody SendEmailRequest request) {
        mailService.sendNotification(request);
        return new ResponseEntity<>(new SimpleResponse(), HttpStatus.CREATED);
    }
}
