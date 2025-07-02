package com.bantvegas.dietplanner.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class OpenAiService {
    @Value("${openai.api.key}")
    private String openAiApiKey;

    @Value("${openai.api.url}")
    private String openAiApiUrl;

    public String getDietPlan(String prompt) {
        RestTemplate restTemplate = new RestTemplate();

        String systemPrompt =
                "You are a professional diet planner. Reply ONLY with valid compact JSON for backend parsing." +
                        " Output a complete 7-day meal plan. Each day has 5 meals (Breakfast, Snack, Lunch, Snack, Dinner)." +
                        " Each meal must include: mealName, calories (integer), and a list of ingredients with quantity in US units (oz, cups, fl oz, pieces, etc, NO grams or ml!)." +
                        " At the end, include shoppingList as an array of objects: {name, amount} (US units for all ingredients)." +
                        " Do NOT add explanations, no extra text, no markdown, just a single valid JSON object with this exact structure:" +
                        " { \"summary\": \"...\", \"days\": [ { \"day\": 1, \"meals\": [ { \"mealName\": \"Breakfast\", \"calories\": 300, \"ingredients\": [ { \"name\": \"Oats\", \"amount\": \"2 oz\" }, ... ] }, ... ] }, ... ], \"shoppingList\": [ { \"name\": \"Oats\", \"amount\": \"60 oz\" }, ... ] }" +
                        " All output in clear ENGLISH, US food terms, US units only.";

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "gpt-4o"); // odporúčam gpt-4o, ak nemáš, použi "gpt-4"
        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", systemPrompt));
        messages.add(Map.of("role", "user", "content", prompt));
        requestBody.put("messages", messages);
        requestBody.put("max_tokens", 5000);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openAiApiKey);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(openAiApiUrl, entity, Map.class);
            if (response.getBody() == null) return "";
            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
            if (choices == null || choices.isEmpty()) return "";
            Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
            if (message == null) return "";
            return (String) message.get("content");
        } catch (Exception e) {
            System.err.println("OpenAI API ERROR: " + e.getMessage());
            return "";
        }
    }
}

