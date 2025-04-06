package com.stride.tracking.identityservice.configuration;

import com.nimbusds.jose.JWSAlgorithm;
import com.stride.tracking.commons.security.SecurityConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@Import({SecurityConfig.class})
public class ApplicationConfig {
    @Bean
    public PasswordEncoder getPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public JWSAlgorithm getJwsAlgorithm() {
        return JWSAlgorithm.HS256;
    }
}
