package com.neeraj.usageservice.service;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.QueryApi;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import com.influxdb.query.FluxRecord;
import com.influxdb.query.FluxTable;
import com.neeraj.kafka.event.AlertingEvent;
import com.neeraj.kafka.event.EnergyUsageEvent;
import com.neeraj.usageservice.client.DeviceClient;
import com.neeraj.usageservice.client.UserClient;
import com.neeraj.usageservice.dto.DeviceDTO;
import com.neeraj.usageservice.dto.UsageDTO;
import com.neeraj.usageservice.dto.UserDTO;
import com.neeraj.usageservice.model.Device;
import com.neeraj.usageservice.model.DeviceWithEnergyUsageForUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;


@Service
@Slf4j
@RequiredArgsConstructor
public class UsageService {

    private final InfluxDBClient influxDBClient;
    private final DeviceClient deviceClient;
    private final UserClient userClient;

    private final KafkaTemplate<String, AlertingEvent> kafkaTemplate;

    @Value("${influxdb.bucket}")
    private String influxDbBucket;

    @Value("${influxdb.org}")
    private String influxDbOrg;

    @KafkaListener(topics = "energy-usage", groupId = "usage-service")
    public void processEnergyUsageEvent(EnergyUsageEvent event) {
//        log.info("Received energy usage event: {}", event);

        Point point = Point.measurement("energy_usage")
                .addTag("deviceId", String.valueOf(event.deviceId()))
                .addField("energyUsage", event.energyUsage())
                .time(event.timestamp(), WritePrecision.MS);

        influxDBClient.getWriteApiBlocking().writePoint(influxDbBucket, influxDbOrg, point);
    }

    /**
     * Main method to aggregate device energy usage and send alerts when thresholds are exceeded.
     * This is a scheduled job that runs every 10 seconds to monitor energy consumption.
     * This method orchestrates the following steps:
     * 1. Fetch energy usage data from InfluxDB for all devices in the last hour
     * 2. Enrich device data with user information by calling device-service
     * 3. Group devices by their userId to calculate total energy per user
     * 4. Fetch user details (email, alert threshold) from user-service
     * 5. Check each user's total energy usage against their threshold and send alerts if exceeded
     *
     * This automated monitoring ensures users are notified in near real-time when their
     * energy consumption exceeds their configured alert thresholds.
     */
    @Scheduled(cron = "*/10 * * * * *")
    public void aggregateDeviceEnergyUsage() {
        // Step 1: Fetch energy usage data from InfluxDB for all devices in the last hour
        List<DeviceWithEnergyUsageForUser> deviceEnergies = fetchDeviceEnergyUsageFromInfluxDB();

        // Step 2: Enrich device data with user information by calling device-service
        enrichDevicesWithUserInfo(deviceEnergies);

        // Step 3: Group devices by their userId to calculate total energy per user
        Map<Long, List<DeviceWithEnergyUsageForUser>> userDeviceEnergiesUsageMap = groupDevicesByUser(deviceEnergies);

        // Step 4: Fetch user details (email, alert threshold) from user-service
        Map<Long, Double> userEnergyThresholdsMap = new HashMap<>();
        Map<Long, String> userEmailMap = new HashMap<>();
        fetchUserThresholdsAndEmails(userDeviceEnergiesUsageMap.keySet(), userEnergyThresholdsMap, userEmailMap);

        // Step 5: Check each user's total energy usage against their threshold and send alerts if exceeded
        checkThresholdsAndSendAlerts(userDeviceEnergiesUsageMap, userEnergyThresholdsMap, userEmailMap);
    }

    /**
     * Fetches aggregated energy usage data from InfluxDB for the last hour.
     * Queries InfluxDB and converts the results into DeviceWithEnergyUsageForUser objects.
     *
     * @return List of devices with their energy usage for the last hour
     */
    private List<DeviceWithEnergyUsageForUser> fetchDeviceEnergyUsageFromInfluxDB() {
        final Instant now = Instant.now();
        final Instant oneHourAgo = now.minusSeconds(3600);

        // Build InfluxDB Flux query to sum energy usage per device for the last hour
        String fluxQuery = String.format("""
                from(bucket: "%s")
                |> range(start: time(v: %s), stop: time(v: %s))
                |> filter(fn: (r) => r["_measurement"] == "energy_usage")
                |> filter(fn: (r) => r["_field"] == "energyUsage")
                |> group(columns: ["deviceId"])
                |> sum(column: "_value")
                """, influxDbBucket, oneHourAgo.toString(), now);

        // Execute the query against InfluxDB
        QueryApi queryApi = influxDBClient.getQueryApi();
        List<FluxTable> tables = queryApi.query(fluxQuery, influxDbOrg);

        // Convert FluxTable records to DeviceWithEnergyUsageForUser objects
        List<DeviceWithEnergyUsageForUser> deviceEnergies = new ArrayList<>();
        for (FluxTable table : tables) {
            for (FluxRecord record : table.getRecords()) {
                String deviceIdStr = (String) record.getValueByKey("deviceId");
                Double energyUsage = record.getValueByKey("_value") instanceof Number ?
                        ((Number) record.getValueByKey("_value")).doubleValue() : 0.0;

                deviceEnergies.add(DeviceWithEnergyUsageForUser.builder()
                        .deviceId(Long.valueOf(deviceIdStr))
                        .energyUsage(energyUsage)
                        .build());
            }
        }
//        log.info("Aggregated device energy usage for the last hour: {}", deviceEnergies);
        return deviceEnergies;
    }

    /**
     * Enriches device energy data with user information by calling the device-service.
     * For each device, fetches the device details and sets the userId.
     * Removes devices that don't have an associated user.
     *
     * @param deviceEnergies List of devices to enrich with user information
     */
    private void enrichDevicesWithUserInfo(List<DeviceWithEnergyUsageForUser> deviceEnergies) {
        // For each device, fetch device details from device-service to get the userId
        for (DeviceWithEnergyUsageForUser deviceEnergy : deviceEnergies) {
            try {
                final DeviceDTO deviceResponse = deviceClient.getDeviceById(deviceEnergy.getDeviceId());
                if (deviceResponse == null || deviceResponse.id() == null) {
                    log.warn("Device not found for deviceId: {}", deviceEnergy.getDeviceId());
                    continue;
                }

                // Set the userId from the device response
                deviceEnergy.setUserId(deviceResponse.userId());
            } catch (Exception e) {
                log.warn("Error fetching device for deviceId: {}", deviceEnergy.getDeviceId(), e);
            }
        }

        // Remove devices that don't have an associated user
        deviceEnergies.removeIf(deviceEnergy -> deviceEnergy.getUserId() == null);
    }

    /**
     * Groups devices by their userId to calculate total energy consumption per user.
     *
     * @param deviceEnergies List of devices with energy usage and user information
     * @return Map of userId to list of devices owned by that user
     */
    private Map<Long, List<DeviceWithEnergyUsageForUser>> groupDevicesByUser(
            List<DeviceWithEnergyUsageForUser> deviceEnergies) {
        // Group devices by userId using Java Streams
        Map<Long, List<DeviceWithEnergyUsageForUser>> userDeviceEnergiesUsageMap =
                deviceEnergies.stream()
                        .collect(Collectors.groupingBy(DeviceWithEnergyUsageForUser::getUserId));
//        log.info("User-device energy usage map: {}", userDeviceEnergiesUsageMap);
        return userDeviceEnergiesUsageMap;
    }

    /**
     * Fetches user details (energy threshold and email) from user-service for all users.
     * Only includes users who have alerts enabled.
     *
     * @param userIds                 Set of user IDs to fetch details for
     * @param userEnergyThresholdsMap Map to populate with userId -> threshold
     * @param userEmailMap            Map to populate with userId -> email
     */
    private void fetchUserThresholdsAndEmails(
            java.util.Set<Long> userIds,
            Map<Long, Double> userEnergyThresholdsMap,
            Map<Long, String> userEmailMap) {

        // For each user, fetch their details from user-service
        for (final Long userId : userIds) {
            try {
                UserDTO user = userClient.getUserById(userId);

                // Skip users who don't exist or don't have alerts enabled
                if (user == null || user.id() == null || !user.alertEnabled()) {
                    log.warn("User not found or alert not enabled for userId: {}", userId);
                    continue;
                }

                // Store the user's energy threshold and email for later use
                userEnergyThresholdsMap.put(userId, user.energyAlertThreshold());
                userEmailMap.put(userId, user.email());
            } catch (Exception e) {
                log.warn("Error fetching user energy threshold for userId: {}", userId, e);
            }
        }
//        log.info("User energy thresholds map: {}", userEnergyThresholdsMap);
//        log.info("User email map: {}", userEmailMap);
    }

    /**
     * Checks each user's total energy usage against their configured threshold.
     * Sends an alert via Kafka if the threshold is exceeded.
     *
     * @param userDeviceEnergiesUsageMap Map of userId to their devices with energy usage
     * @param userEnergyThresholdsMap    Map of userId to their energy alert threshold
     * @param userEmailMap               Map of userId to their email address
     */
    private void checkThresholdsAndSendAlerts(
            Map<Long, List<DeviceWithEnergyUsageForUser>> userDeviceEnergiesUsageMap,
            Map<Long, Double> userEnergyThresholdsMap,
            Map<Long, String> userEmailMap) {

        // Iterate through all users who have thresholds configured
        for (final Long userId : userEnergyThresholdsMap.keySet()) {
            final Double threshold = userEnergyThresholdsMap.get(userId);
            final List<DeviceWithEnergyUsageForUser> devices = userDeviceEnergiesUsageMap.get(userId);

            // Calculate total energy usage by summing all devices for this user
            final Double totalEnergyUsage = devices.stream()
                    .mapToDouble(DeviceWithEnergyUsageForUser::getEnergyUsage)
                    .sum();

            // Check if user has exceeded their threshold
            if (totalEnergyUsage > threshold) {
                log.info("ALERT: User with ID {} has exceeded energy threshold! Total Energy Usage: {}, User's Threshold: {}",
                        userId, totalEnergyUsage, threshold);

                // Create an alert event with user details and energy usage information
                final AlertingEvent alertingEvent = AlertingEvent.builder()
                        .userId(userId)
                        .message("ALERT: Energy usage exceeded threshold")
                        .threshold(threshold)
                        .totalEnergyUsage(totalEnergyUsage)
                        .email(userEmailMap.get(userId))
                        .build();

                // Send the alert event to Kafka topic for processing by alerting service
                kafkaTemplate.send("energy-alerts", alertingEvent);
            } else {
                log.info("User with ID {} has yet not exceeded energy threshold. Total Energy Usage: {}, User's Threshold: {}",
                        userId, totalEnergyUsage, threshold);
            }
        }
    }

    /**
     * Main method to get energy usage data for a specific user over a specified number of days.
     * This method orchestrates the following steps:
     * 1. Fetch all devices owned by the user
     * 2. Query InfluxDB for aggregated energy usage per device
     * 3. Combine device information with their energy consumption data
     * 4. Return a complete usage report
     *
     * @param userId The ID of the user to fetch usage data for
     * @param days   Number of days to look back for energy usage data
     * @return UsageDTO containing user ID and list of devices with their energy consumption
     */
    public UsageDTO getXDaysUsageForUser(Long userId, int days) {
        log.info("Getting usage for userId {} over past {} days", userId, days);

        // Step 1: Fetch all devices owned by the user from device-service
        List<Device> devices = fetchAndConvertUserDevices(userId);

        // If user has no devices, return early with empty result
        if (devices.isEmpty()) {
            log.warn("No devices found for userId: {}", userId);
            return buildEmptyUsageDTO(userId);
        }

        // Step 2: Query InfluxDB to get aggregated energy consumption for each device
        Map<Long, Double> deviceEnergyMap = queryDeviceEnergyUsage(devices, days);

        // Step 3: Populate each device with its energy consumption from InfluxDB results
        populateDevicesWithEnergyData(devices, deviceEnergyMap);

        // Step 4: Convert devices to DTOs and build the final response
        return buildUsageDTO(userId, devices);
    }

    /**
     * Fetches all devices for a user from device-service and converts them to Device entities.
     * Filters out any devices with null IDs.
     *
     * @param userId The ID of the user whose devices to fetch
     * @return List of Device entities owned by the user
     */
    private List<Device> fetchAndConvertUserDevices(Long userId) {
        // Call device-service to get all devices for this user
        final List<DeviceDTO> devicesDto = deviceClient.getAllDevicesForUser(userId);

        // Convert DeviceDTO objects to Device entities for internal processing
        final List<Device> devices = new ArrayList<>();
        for (DeviceDTO deviceDto : devicesDto) {
            // Skip devices without valid IDs
            if (deviceDto.id() == null) {
                log.warn("Skipping device with null ID for userId: {}", userId);
                continue;
            }

            devices.add(Device.builder()
                    .id(deviceDto.id())
                    .name(deviceDto.name())
                    .type(deviceDto.type())
                    .location(deviceDto.location())
                    .userId(deviceDto.userId())
                    .build());
        }

        return devices;
    }

    /**
     * Queries InfluxDB to get aggregated energy consumption for each device over the specified time period.
     * Builds a Flux query that filters by device IDs and sums energy usage within the time range.
     *
     * @param devices List of devices to query energy data for
     * @param days    Number of days to look back
     * @return Map of deviceId to total energy consumed (in kWh)
     */
    private Map<Long, Double> queryDeviceEnergyUsage(List<Device> devices, int days) {
        // Calculate the time range for the query
        final Instant now = Instant.now();
        final Instant start = now.minusSeconds((long) days * 24 * 3600);

        // Build the Flux query to fetch energy data from InfluxDB
        String fluxQuery = buildFluxQueryForDevices(devices, start, now);

        // Execute the query and parse results into a map
        return executeFluxQueryAndAggregateResults(fluxQuery, devices.size());
    }

    /**
     * Builds a Flux query string to fetch energy usage data for specific devices within a time range.
     * The query filters by measurement name, field name, and device IDs, then groups and sums by device.
     *
     * @param devices List of devices to include in the query
     * @param start   Start time for the query range
     * @param now     End time for the query range
     * @return Formatted Flux query string ready to execute
     */
    private String buildFluxQueryForDevices(List<Device> devices, Instant start, Instant now) {
        // Extract device IDs and convert to strings for use in Flux query
        List<String> deviceIdStrings = devices.stream()
                .map(Device::getId)
                .filter(Objects::nonNull)
                .map(String::valueOf)
                .toList();

        // Build device filter: r["deviceId"] == "1" or r["deviceId"] == "2" or ...
        final String deviceFilter = deviceIdStrings.stream()
                .map(idStr -> String.format("r[\"deviceId\"] == \"%s\"", idStr))
                .collect(Collectors.joining(" or "));

        // Construct the complete Flux query
        // This query:
        // 1. Selects data from the specified bucket
        // 2. Filters by time range (start to now)
        // 3. Filters for "energy_usage" measurement and "energyUsage" field
        // 4. Filters for specific device IDs
        // 5. Groups by deviceId and sums the energy values
        return String.format("""
                from(bucket: "%s")
                  |> range(start: time(v: "%s"), stop: time(v: "%s"))
                  |> filter(fn: (r) => r["_measurement"] == "energy_usage")
                  |> filter(fn: (r) => r["_field"] == "energyUsage")
                  |> filter(fn: (r) => %s)
                  |> group(columns: ["deviceId"])
                  |> sum(column: "_value")
                """, influxDbBucket, start.toString(), now.toString(), deviceFilter);
    }

    /**
     * Executes the Flux query against InfluxDB and parses the results into a map.
     * Each FluxRecord contains a deviceId and aggregated energy value.
     *
     * @param fluxQuery    The Flux query to execute
     * @param deviceCount  Number of devices being queried (for logging)
     * @return Map of deviceId to total energy consumed
     */
    private Map<Long, Double> executeFluxQueryAndAggregateResults(String fluxQuery, int deviceCount) {
        final Map<Long, Double> aggregatedMap = new HashMap<>();

        try {
            // Execute the Flux query
            QueryApi queryApi = influxDBClient.getQueryApi();
            List<FluxTable> tables = queryApi.query(fluxQuery, influxDbOrg);

            // Parse each table and record to extract deviceId and energy values
            for (FluxTable table : tables) {
                for (FluxRecord record : table.getRecords()) {
                    // Extract deviceId from the record
                    Object deviceIdObj = record.getValueByKey("deviceId");
                    String deviceIdStr = deviceIdObj == null ? null : deviceIdObj.toString();
                    if (deviceIdStr == null) {
                        log.warn("Found record with null deviceId, skipping");
                        continue;
                    }

                    // Extract energy value from the record
                    Double energyConsumed = record.getValueByKey("_value") instanceof Number
                            ? ((Number) record.getValueByKey("_value")).doubleValue()
                            : 0.0;

                    try {
                        // Store the aggregated energy for this device
                        Long deviceId = Long.valueOf(deviceIdStr);
                        aggregatedMap.put(deviceId, aggregatedMap.getOrDefault(deviceId, 0.0) + energyConsumed);
                    } catch (NumberFormatException nfe) {
                        log.warn("Failed to parse deviceId from flux record: {}", deviceIdStr, nfe);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Failed to query InfluxDB: {}", e.getMessage(), e);
            // Return empty map on error - devices will get 0.0 energy by default
        }

        return aggregatedMap;
    }

    /**
     * Populates each device with its energy consumption data from the aggregated map.
     * Devices not found in the map will have their energy set to 0.0.
     *
     * @param devices         List of devices to populate
     * @param deviceEnergyMap Map of deviceId to energy consumption
     */
    private void populateDevicesWithEnergyData(List<Device> devices, Map<Long, Double> deviceEnergyMap) {
        // Set energy consumed for each device from the aggregated results
        for (Device device : devices) {
            if (device == null || device.getId() == null) {
                log.warn("Skipping null device or device with null ID");
                continue;
            }

            // Get energy from map, default to 0.0 if device has no usage data
            Double energyConsumed = deviceEnergyMap.getOrDefault(device.getId(), 0.0);
            device.setEnergyConsumed(energyConsumed);
        }
    }

    /**
     * Builds the final UsageDTO response containing user ID and devices with their energy data.
     * Converts Device entities back to DeviceDTO for the API response.
     *
     * @param userId  The user ID
     * @param devices List of devices with populated energy data
     * @return UsageDTO ready to be returned to the client
     */
    private UsageDTO buildUsageDTO(Long userId, List<Device> devices) {
        // Convert Device entities to DeviceDTO objects for the API response
        final List<DeviceDTO> resultDevices = devices.stream()
                .map(d -> DeviceDTO.builder()
                        .id(d.getId())
                        .name(d.getName())
                        .type(d.getType())
                        .location(d.getLocation())
                        .userId(d.getUserId())
                        .energyConsumed(d.getEnergyConsumed())
                        .build())
                .toList();


        // Build and return the final UsageDTO
        return UsageDTO.builder()
                .userId(userId)
                .devices(resultDevices)
                .build();
    }

    /**
     * Builds an empty UsageDTO when a user has no devices.
     *
     * @param userId The user ID
     * @return UsageDTO with empty device list
     */
    private UsageDTO buildEmptyUsageDTO(Long userId) {
        return UsageDTO.builder()
                .userId(userId)
                .devices(List.of())
                .build();
    }
}
