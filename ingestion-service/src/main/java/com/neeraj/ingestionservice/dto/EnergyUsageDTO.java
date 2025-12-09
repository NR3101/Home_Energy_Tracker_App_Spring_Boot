package com.neeraj.ingestionservice.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;

import java.time.Instant;


@Builder
public record EnergyUsageDTO(
        Long deviceId,
        Double energyUsage,
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        Instant timestamp
) {
}
