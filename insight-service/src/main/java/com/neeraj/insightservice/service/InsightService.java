package com.neeraj.insightservice.service;
import com.neeraj.insightservice.client.UsageClient;
import com.neeraj.insightservice.dto.DeviceDTO;
import com.neeraj.insightservice.dto.InsightDTO;
import com.neeraj.insightservice.dto.UsageDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.stereotype.Service;
@Service
@Slf4j
@RequiredArgsConstructor
public class InsightService {
    private final UsageClient usageClient;
    private final OllamaChatModel ollamaChatModel;
    public InsightDTO getSavingTips(Long userId) {
        final UsageDTO usageData = usageClient.getXDaysUsageForUser(userId, 5);
        // Handle null or empty devices list
        if (usageData.devices() == null || usageData.devices().isEmpty()) {
            log.warn("No devices found for user {} or unable to fetch usage data", userId);
            return InsightDTO.builder()
                    .userId(userId)
                    .savingTips("No device data available. Please add devices to your account to get energy-saving tips.")
                    .totalEnergyUsage(0.0)
                    .build();
        }
        Double totalEnergyConsumed = usageData.devices().stream()
                .mapToDouble(DeviceDTO::energyConsumed)
                .sum();
        log.info("Generating energy-saving tips for user {} with total energy usage: {} kWh", userId, totalEnergyConsumed);
        String prompt = String.format("""
                        ENERGY CONSUMPTION DATA:
                        - Time Period: Last 5 days
                        - Total Energy Used: %.2f kWh
                        - Number of Devices: %d
                        TASK: Provide 4-5 practical energy-saving tips for a home with this consumption level.
                        Format as a numbered list. Each tip should:
                        - Be specific and actionable
                        - Include estimated potential savings (in kWh or percentage)
                        - Be realistic for a typical household
                        Keep your response concise and practical.
                        """,
                totalEnergyConsumed,
                usageData.devices().size()
        );
        ChatResponse response = ollamaChatModel.call(new Prompt(prompt));
        String aiResponse = response.getResult().getOutput().getText();
        return InsightDTO.builder()
                .userId(userId)
                .savingTips(aiResponse)
                .totalEnergyUsage(totalEnergyConsumed)
                .build();
    }
    public InsightDTO getOverview(Long userId) {
        final UsageDTO usageData = usageClient.getXDaysUsageForUser(userId, 5);
        // Handle null or empty devices list
        if (usageData.devices() == null || usageData.devices().isEmpty()) {
            log.warn("No devices found for user {} or unable to fetch usage data", userId);
            return InsightDTO.builder()
                    .userId(userId)
                    .savingTips("No device data available. Please add devices to your account to get energy usage overview.")
                    .totalEnergyUsage(0.0)
                    .build();
        }
        Double totalEnergyConsumed = usageData.devices().stream()
                .mapToDouble(DeviceDTO::energyConsumed)
                .sum();
        log.info("Generating energy overview for user {} with total energy usage: {} kWh", userId, totalEnergyConsumed);
        // Build detailed device breakdown for better insights
        StringBuilder deviceBreakdown = new StringBuilder();
        usageData.devices().forEach(device ->
                deviceBreakdown.append(String.format(
                        "- %s (%s) in %s: %.2f kWh\n",
                        device.name(),
                        device.type(),
                        device.location(),
                        device.energyConsumed()
                ))
        );
        String prompt = String.format("""
                        Analyze the following energy consumption data and provide actionable insights:
                        ENERGY DATA:
                        - Period: Last 5 days
                        - Total Energy: %.2f kWh
                        - Number of Devices: %d
                        DEVICE BREAKDOWN:
                        %s
                        Provide a brief analysis with:
                        1. Overall Assessment (1-2 sentences on total consumption level)
                        2. Top 3 Energy Consumers (list devices with highest consumption)
                        3. Key Recommendations (3-4 specific actions to reduce energy usage)
                        Be concise, specific, and focus only on the devices listed above.
                        """,
                totalEnergyConsumed,
                usageData.devices().size(),
                deviceBreakdown.toString()
        );
        ChatResponse response = ollamaChatModel.call(new Prompt(prompt));
        String aiResponse = response.getResult().getOutput().getText();
        return InsightDTO.builder()
                .userId(userId)
                .savingTips(aiResponse)
                .totalEnergyUsage(totalEnergyConsumed)
                .build();
    }
}
