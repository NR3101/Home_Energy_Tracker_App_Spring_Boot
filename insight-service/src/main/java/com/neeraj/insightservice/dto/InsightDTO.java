package com.neeraj.insightservice.dto;

import lombok.Builder;

@Builder
public record InsightDTO(
        Long userId,
        String savingTips,
        Double totalEnergyUsage
) {
}
