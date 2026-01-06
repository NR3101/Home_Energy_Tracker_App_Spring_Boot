package com.neeraj.insightservice.client;

import com.neeraj.insightservice.dto.UsageDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@Component
public class UsageClient {
    private final RestTemplate restTemplate;
    private final String baseUrl;

    public UsageClient(@Value("${usage.service.url}") String baseUrl) {
        this.restTemplate = new RestTemplate();
        this.baseUrl = baseUrl;
    }

    public UsageDTO getXDaysUsageForUser(Long userId, int days) {
        String url = UriComponentsBuilder.fromUriString(baseUrl)
                .path("/{userId}")
                .queryParam("days", days)
                .buildAndExpand(userId)
                .toUriString();

        try {
            ResponseEntity<UsageDTO> response = restTemplate.getForEntity(url, UsageDTO.class);
            return response.getBody();
        } catch (Exception e) {
            log.error("Error calling usage-service for user {}: {}", userId, e.getMessage());
            return UsageDTO.builder()
                    .userId(userId)
                    .devices(null)
                    .build();
        }
    }
}
