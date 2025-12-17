package com.neeraj.usageservice.dto;


public record DeviceDTO(
        Long id,
        String name,
        String type,
        String location,
        Long userId) {
}
