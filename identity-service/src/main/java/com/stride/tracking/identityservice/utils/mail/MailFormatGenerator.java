package com.stride.tracking.identityservice.utils.mail;

import java.util.Map;

public interface MailFormatGenerator {
    String generate(Map<String, String> properties);
    String getSubject();
}
