package com.stride.tracking.notificationservice.client;

import com.stride.tracking.dto.request.EmailRequest;
import jakarta.mail.*;
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Properties;

@Component
@RequiredArgsConstructor
@Slf4j
public class MailSender {
    @Value("${mail.gmail.username}")
    private String GMAIL_USERNAME;
    @Value("${mail.gmail.password}")
    private String GMAIL_PASSWORD;

    @Async
    public void sendMail(EmailRequest request) {
        Session session = setUpSession();

        try {
            Address[] addresses = request.getTo().stream().map(recipient -> {
                try {
                    return new InternetAddress(recipient.getEmail());
                } catch (AddressException e) {
                    throw new RuntimeException(e);
                }
            }).toArray(Address[]::new);

            Message message = setUpMessageForMail(session, request.getSubject(), request.getHtmlContent(), addresses);
            Transport.send(message);
        } catch (Exception e) {
            log.error("Error sending email: {}", e.getMessage(), e);
        }
    }

    private Session setUpSession() {
        Properties prop = new Properties();
        prop.put("mail.smtp.host", "smtp.gmail.com");
        prop.put("mail.smtp.port", "465");
        prop.put("mail.smtp.auth", "true");
        prop.put("mail.smtp.socketFactory.port", "465");
        prop.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        prop.put("mail.smtp.ssl.checkserveridentity", "true");

        return Session.getInstance(prop, new jakarta.mail.Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(GMAIL_USERNAME, GMAIL_PASSWORD);
            }
        });
    }

    private Message setUpMessageForMail(Session session, String title, String body, Address[] addresses) throws
            MessagingException {
        Message message = new MimeMessage(session);

        message.setFrom(new InternetAddress("from@gmail.com"));
        message.setSubject(title);
        message.setText(body);
        message.setContent(body, "text/html;charset=utf-8");

        if (addresses.length > 1) {
            message.setRecipients(Message.RecipientType.BCC, addresses);
        } else {
            message.setRecipient(Message.RecipientType.TO, addresses[0]);
        }

        return message;
    }
}
