package com.neeraj.usageservice.client;

import com.neeraj.usageservice.dto.DeviceDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
public class DeviceClient {
    private final RestTemplate restTemplate;
    private final String baseUrl;

    public DeviceClient(@Value("${device.service.url}") String baseUrl) {
        this.restTemplate = new RestTemplate();
        this.baseUrl = baseUrl;
    }

    public DeviceDTO getDeviceById(Long deviceId) {
        String url = UriComponentsBuilder.fromUriString(baseUrl)
                .path("/api/v1/device/{deviceId}")
                .buildAndExpand(deviceId)
                .toUriString();

        log.debug("Calling device-service: {}", url);

        try {
            ResponseEntity<DeviceDTO> response = restTemplate.getForEntity(url, DeviceDTO.class);
            return response.getBody();
        } catch (Exception e) {
            log.error("Error fetching device {}: {}", deviceId, e.getMessage());
            return null;
        }
    }

    public List<DeviceDTO> getAllDevicesForUser(Long userId) {
        String url = UriComponentsBuilder.fromUriString(baseUrl)
                .path("/api/v1/device/user/{userId}")
                .buildAndExpand(userId)
                .toUriString();

        try {
            ResponseEntity<DeviceDTO[]> response = restTemplate.getForEntity(url, DeviceDTO[].class);
            DeviceDTO[] devicesArray = response.getBody();

            if (devicesArray == null) {
                log.warn("Received null response from device-service for user {}", userId);
                return List.of();
            }

            return Arrays.asList(devicesArray);
        } catch (Exception e) {
            log.error("Error fetching devices for user {}: {}", userId, e.getMessage());
            return List.of();
        }
    }
}
