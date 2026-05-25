package com.civicsense.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class SMSService {

    // Inject Fast2SMS API Key from application.properties
    @Value("${fast2sms.api.key}")
    private String apiKey;

    private static final String SEND_URL = "https://www.fast2sms.com/dev/bulkV2";

    // Store last API response for debugging
    private String lastApiResponse;

    // =========================
    // Send SMS via Fast2SMS V2
    // =========================
    public boolean sendSms(String to, String messageBody) {
        try {
            RestTemplate restTemplate = new RestTemplate();

            // Prepare headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("authorization", apiKey);

            // Prepare body
            Map<String, Object> body = new HashMap<>();
            body.put("route", "q");                // ✅ Quick SMS route
            body.put("sender_id", "TXTIND");       // Sender ID
            body.put("message", messageBody);      
            body.put("language", "english");       
            body.put("flash", 0);                  
            body.put("numbers", to);               // 10 digit number only (NO +91, NO 91)

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

            // Send POST request
            ResponseEntity<String> response =
                    restTemplate.postForEntity(SEND_URL, request, String.class);

            // Store response for debugging
            lastApiResponse = response.getBody();
            System.out.println("SMS Response: " + lastApiResponse);

            return response.getStatusCode() == HttpStatus.OK &&
                    response.getBody() != null &&
                    response.getBody().toLowerCase().contains("success");

        } catch (Exception e) {
            e.printStackTrace();
            lastApiResponse = "Exception occurred: " + e.getMessage();
            return false;
        }
    }

    // =========================
    // Get last API response
    // =========================
    public String getLastApiResponse() {
        return lastApiResponse;
    }
}