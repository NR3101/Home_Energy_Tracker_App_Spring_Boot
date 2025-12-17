package com.neeraj.usageservice.client;

import com.neeraj.usageservice.dto.UserDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class UserClient {
    private final RestTemplate restTemplate;
    private final String baseUrl;

    public UserClient(@Value("${user.service.url}") String baseUrl) {
        this.restTemplate = new RestTemplate();
        this.baseUrl = baseUrl;
    }

    public UserDTO getUserById(Long userId) {
        String url = UriComponentsBuilder.fromUriString(baseUrl)
                .path("/api/v1/user/{userId}")
                .buildAndExpand(userId)
                .toUriString();

        ResponseEntity<UserDTO> response = restTemplate.getForEntity(url, UserDTO.class);
        return response.getBody();
    }
}
