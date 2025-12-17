package com.neeraj.usageservice.dto;

import lombok.Builder;


@Builder
public record UserDTO (
    Long id,
    String firstName,
    String lastName,
    String email,
    String address,
    Boolean alertEnabled,
    Double energyAlertThreshold
) {}
