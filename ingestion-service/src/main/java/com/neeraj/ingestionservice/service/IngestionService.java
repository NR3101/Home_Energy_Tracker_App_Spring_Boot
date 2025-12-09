package com.neeraj.ingestionservice.service;

import com.neeraj.ingestionservice.dto.EnergyUsageDTO;
import com.neeraj.kafka.event.EnergyUsageEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class IngestionService {

    private final KafkaTemplate<String, EnergyUsageEvent> kafkaTemplate;

    public void ingestEnergyUsage(EnergyUsageDTO usageDTO) {
        // Convert DTO to Event
        EnergyUsageEvent event = EnergyUsageEvent.builder()
                .deviceId(usageDTO.deviceId())
                .energyUsage(usageDTO.energyUsage())
                .timestamp(usageDTO.timestamp())
                .build();

        // Send Event to Kafka
        kafkaTemplate.send("energy-usage", event);
        log.info("Ingested energy usage event {}", event);
    }
}
