package com.neeraj.deviceservice.dto;

import com.neeraj.deviceservice.model.DeviceType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DeviceDTO {
    private Long id;
    private String name;
    private DeviceType type;
    private String location;
    private Long userId;
}
