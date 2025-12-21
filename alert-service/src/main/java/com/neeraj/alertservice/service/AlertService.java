package com.neeraj.alertservice.service;

import com.neeraj.kafka.event.AlertingEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class AlertService {

    private final EmailService emailService;

    @KafkaListener(topics = "energy-alerts", groupId = "alert-service")
    public void processEnergyUsageAlert(AlertingEvent alertEvent) {
        log.info("Received alerting alertEvent: {}", alertEvent);

        // Send email
        String subject = "Energy Usage Alert for User " + alertEvent.userId();
        String body = "Dear User,\n\nYour energy usage has exceeded the threshold of " + alertEvent.threshold() + " kWh.\n\nTotal energy usage: " + alertEvent.totalEnergyUsage() + " kWh.\n\nBest regards,\nEnergy Tracker Team";
        emailService.sendEmail(alertEvent.email(), subject, body, alertEvent.userId());
    }
}
