package com.neeraj.deviceservice.service;

import com.neeraj.deviceservice.dto.DeviceDTO;
import com.neeraj.deviceservice.entity.Device;
import com.neeraj.deviceservice.exception.DeviceNotFoundException;
import com.neeraj.deviceservice.repository.DeviceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class DeviceService {

    private final DeviceRepository deviceRepository;

    public DeviceDTO getDevice(Long id) {
        Device device = deviceRepository.findById(id).orElseThrow(() -> new DeviceNotFoundException("Device not found with id: " + id));

        return toDeviceDTO(device);
    }

    public DeviceDTO createDevice(DeviceDTO deviceDTO) {
        Device device = Device.builder()
                .name(deviceDTO.getName())
                .type(deviceDTO.getType())
                .location(deviceDTO.getLocation())
                .userId(deviceDTO.getUserId())
                .build();

        final Device savedDevice = deviceRepository.save(device);
        return toDeviceDTO(savedDevice);
    }

    public DeviceDTO updateDevice(Long id, DeviceDTO deviceDTO) {
        Device device = deviceRepository.findById(id).orElseThrow(() -> new DeviceNotFoundException("Device not found with id: " + id));

        device.setName(deviceDTO.getName());
        device.setType(deviceDTO.getType());
        device.setLocation(deviceDTO.getLocation());
        device.setUserId(deviceDTO.getUserId());

        final Device updatedDevice = deviceRepository.save(device);
        return toDeviceDTO(updatedDevice);
    }

    public void deleteDevice(Long id) {
        Device device = deviceRepository.findById(id).orElseThrow(() -> new DeviceNotFoundException("Device not found with id: " + id));

        deviceRepository.delete(device);
    }

    private DeviceDTO toDeviceDTO(Device device) {
        return DeviceDTO.builder()
                .id(device.getId())
                .name(device.getName())
                .type(device.getType())
                .location(device.getLocation())
                .userId(device.getUserId())
                .build();
    }


    public List<DeviceDTO> getAllDevicesForUser(Long userId) {
        List<Device> devices = deviceRepository.findAllByUserId(userId);
        return devices.stream().map(this::toDeviceDTO).toList();
    }
}
