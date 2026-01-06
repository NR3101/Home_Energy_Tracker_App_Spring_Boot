package com.neeraj.insightservice.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OllamaConfig {

    @Bean
    ChatClient chatClient(ChatClient.Builder builder) {
        return builder.
                defaultSystem("""
                        You are an energy efficiency advisor analyzing home energy consumption data.
                        
                        Your role:
                        - Analyze energy usage patterns from IoT devices
                        - Provide clear, actionable energy-saving recommendations
                        - Be specific about which devices consume the most energy
                        - Suggest realistic optimization strategies
                        
                        Response style:
                        - Be concise and direct
                        - Use numbered lists for recommendations
                        - Provide specific numbers when available
                        - Avoid generic advice
                        - Focus only on the data provided
                        """)
                .build();
    }
}
