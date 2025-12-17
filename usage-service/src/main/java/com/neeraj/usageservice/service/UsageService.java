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
import com.neeraj.usageservice.dto.UserDTO;
import com.neeraj.usageservice.model.DeviceWithEnergyUsageForUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    // Scheduled cron job that runs every 10 seconds to check energy usage and send alerts if needed
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
     * @param userIds Set of user IDs to fetch details for
     * @param userEnergyThresholdsMap Map to populate with userId -> threshold
     * @param userEmailMap Map to populate with userId -> email
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
     * @param userEnergyThresholdsMap Map of userId to their energy alert threshold
     * @param userEmailMap Map of userId to their email address
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
}
