package com.neeraj.deviceservice;

import com.neeraj.deviceservice.entity.Device;
import com.neeraj.deviceservice.model.DeviceType;
import com.neeraj.deviceservice.repository.DeviceRepository;
import com.neeraj.deviceservice.service.DeviceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@Slf4j
@SpringBootTest
class DeviceServiceApplicationTests {

    @Autowired
    private DeviceRepository deviceRepository;
    public static final int NO_OF_DEVICES = 100;

    @Test
    void contextLoads() {
    }

    @Disabled
    @Test
    void createDevices() {
        for (int i = 1; i <= NO_OF_DEVICES; i++) {
            Device device = Device.builder()
                    .name("Device " + i)
                    .type(DeviceType.values()[i % DeviceType.values().length])
                    .location("Room " + (i % 10 + 1))
                    .userId((long) ((i % 10) + 1))
                    .build();
            deviceRepository.save(device);
        }

        log.info("Created {} devices in the database", NO_OF_DEVICES);
    }


}
