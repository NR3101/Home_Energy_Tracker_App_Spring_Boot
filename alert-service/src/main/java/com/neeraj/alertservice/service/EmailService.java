package com.neeraj.alertservice.service;

import com.neeraj.alertservice.entity.Alert;
import com.neeraj.alertservice.repository.AlertRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender javaMailSender;
    private final AlertRepository alertRepository;

    public void sendEmail(String recipient, String subject, String body, Long userId) {
        log.info("Sending email to {} for user {}", recipient, userId);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(recipient);
        message.setFrom("energy-tracker@neeraj.com");
        message.setSubject(subject);
        message.setText(body);

        try {
            javaMailSender.send(message);

            // Save alert success state to database
            final Alert sentAlert = Alert.builder()
                    .userId(userId)
                    .sent(true)
                    .createdAt(LocalDateTime.now())
                    .build();

            alertRepository.saveAndFlush(sentAlert);
        } catch (MailException e) {
            log.error("Error sending email to {} for user {}", recipient, userId, e);

            // Save alert failure state to database
            final Alert failedAlert = Alert.builder()
                    .userId(userId)
                    .sent(false)
                    .createdAt(LocalDateTime.now())
                    .build();

            alertRepository.saveAndFlush(failedAlert);
        }

        log.info("Email sent to {} for user {}", recipient, userId);
    }
}
