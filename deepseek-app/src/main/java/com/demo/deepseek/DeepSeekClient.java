package com.demo.deepseek;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;

public class DeepSeekClient {

    static final String DEFAULT_BASE_URL = "https://api.deepseek.ai";
    private static final Duration TIMEOUT = Duration.ofSeconds(30);

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String apiKey;
    private final String baseUrl;

    public DeepSeekClient(DeepSeekModels.Config config) {
        this.apiKey = config.apiKey();
        this.baseUrl = config.baseUrl();
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(TIMEOUT)
            .build();
        this.objectMapper = new ObjectMapper();
    }

    public DeepSeekModels.ChatResponse chat(String model, String prompt) throws DeepSeekException {
        var request =  new DeepSeekModels.ChatRequests(
                model,
                List.of(new DeepSeekModels.Message("user", prompt))
        );

        try {
            var httpRequest = HttpRequest.newBuilder()
                .uri( URI.create(baseUrl + "/chat"))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .POST( HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(request)))
                .build();

            var response = httpClient.send(httpRequest, BodyHandlers.ofString());    

            if ( response.statusCode() != 200){
                throw new DeepSeekException("API request failed with status code: " + response.statusCode() +
                     " body: " + response.body());
            }

            return objectMapper.readValue(response.body(), DeepSeekModels.ChatResponse.class );

        } catch (IOException e) {
            throw new DeepSeekException("Failed to process request/response", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new DeepSeekException("Request was interrupted", e);
        }
        
        
    }


}
