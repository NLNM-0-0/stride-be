package com.stride.tracking.notificationservice.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

@Configuration
@ConfigurationProperties(prefix = "firebase")
@Getter
@Setter
public class FirebaseProperties {
    private Resource serviceAccount;
}
