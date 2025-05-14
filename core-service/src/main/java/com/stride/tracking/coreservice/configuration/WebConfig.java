package com.stride.tracking.coreservice.configuration;

import com.stride.tracking.coreservice.utils.converter.ProgressTimeFrameConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {
    private final ProgressTimeFrameConverter progressTimeFrameConverter;

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(progressTimeFrameConverter);
    }
}
