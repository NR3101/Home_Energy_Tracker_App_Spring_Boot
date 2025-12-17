package com.neeraj.usageservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DeviceWithEnergyUsageForUser {
    private Long deviceId;
    private Double energyUsage;
    private Long userId;
}
