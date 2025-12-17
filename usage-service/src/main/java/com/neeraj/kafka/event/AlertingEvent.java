package com.neeraj.kafka.event;

import lombok.Builder;

@Builder
public record AlertingEvent(
        Long userId,
        String message,
        Double threshold,
        Double totalEnergyUsage,
        String email
) {
}
