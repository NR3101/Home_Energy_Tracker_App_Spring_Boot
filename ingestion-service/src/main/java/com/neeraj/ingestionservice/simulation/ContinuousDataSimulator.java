package com.neeraj.ingestionservice.simulation;

import com.neeraj.ingestionservice.dto.EnergyUsageDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Random;

@Component
@Slf4j
@RequiredArgsConstructor
public class ContinuousDataSimulator implements CommandLineRunner {
    private final RestTemplate restTemplate = new RestTemplate();
    private final Random random = new Random();

    @Value("${simulation.requests-per-interval}")
    private int requestsPerInterval;

    @Value("${simulation.endpoint}")
    private String ingestionEndpoint;

    @Override
    public void run(String... args) throws Exception {
        log.info("Starting continuous data simulation");
    }

//    @Scheduled(fixedDelayString = "${simulation.fixedDelay}")
    public void sendMockData() {
        for (int i = 0; i < requestsPerInterval; i++) {
            EnergyUsageDTO usageDTO = EnergyUsageDTO.builder()
                    .deviceId(random.nextLong(1, 10))
                    .energyUsage(Math.round(random.nextDouble(0.0, 5.0) * 100.0) / 100.0)
                    .timestamp(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant())
                    .build();

            try {
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                HttpEntity<EnergyUsageDTO> request = new HttpEntity<>(usageDTO, headers);
                restTemplate.postForEntity(ingestionEndpoint, request, Void.class);

                log.info("Sent mock data {}", usageDTO);
            } catch (Exception e) {
                log.error("Error sending mock data: {}", e.getMessage());
            }
        }
    }
}
