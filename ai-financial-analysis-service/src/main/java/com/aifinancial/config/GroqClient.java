package com.aifinancial.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class GroqClient {

    @Value("${groq.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private static final String GROQ_URL = "https://api.groq.com/openai/v1/chat/completions";

    public GroqResponse chatCompletion(String prompt) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        GroqRequest request = new GroqRequest(
            "llama-3.3-70b-versatile",
            List.of(new GroqMessage("user", prompt)),
            0.7,
            2000
        );

        HttpEntity<GroqRequest> entity = new HttpEntity<>(request, headers);
        return restTemplate.postForObject(GROQ_URL, entity, GroqResponse.class);
    }

    // DTOs for Groq API
    public static class GroqRequest {
        public String model;
        public List<GroqMessage> messages;
        public Double temperature;
        public Integer max_tokens;

        public GroqRequest(String model, List<GroqMessage> messages, Double temperature, Integer max_tokens) {
            this.model = model;
            this.messages = messages;
            this.temperature = temperature;
            this.max_tokens = max_tokens;
        }
    }

    public static class GroqMessage {
        public String role;
        public String content;

        public GroqMessage(String role, String content) {
            this.role = role;
            this.content = content;
        }
    }

    public static class GroqResponse {
        public List<Choice> choices;
        
        public static class Choice {
            public GroqMessage message;
        }
    }
}
