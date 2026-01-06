package com.neeraj.insightservice.dto;

import lombok.Builder;

@Builder
public record DeviceDTO(
        Long id,
        String name,
        String type,
        String location,
        Double energyConsumed
) {
}
