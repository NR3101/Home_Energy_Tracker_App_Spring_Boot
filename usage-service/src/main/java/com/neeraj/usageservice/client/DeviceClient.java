package com.neeraj.usageservice.client;

import com.neeraj.usageservice.dto.DeviceDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

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

        ResponseEntity<DeviceDTO> response = restTemplate.getForEntity(url, DeviceDTO.class);
        return response.getBody();
    }
}
