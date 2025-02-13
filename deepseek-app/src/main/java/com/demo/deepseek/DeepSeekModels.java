package com.demo.deepseek;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DeepSeekModels {

    public record Config(String apiKey, String baseUrl) {
        public Config{
            if(apiKey == null || apiKey.isBlank()){
                throw new IllegalArgumentException("Api Key is required, cannot be null or empty");
            }            
            baseUrl = baseUrl == null || baseUrl.isBlank() ? DeepSeekClient.DEFAULT_BASE_URL : baseUrl;
        }
    }

    public record Message(String role, String content) {}
    public record ChatRequests(String model, List<Message> messages) {}    
    public record ChatResponse( List<Choise> choices, Usage usage) {}
    public record Choise( @JsonProperty("message") Message message){}
    public record Usage(
        @JsonProperty("prompt_tokens") int promptTokens,
        @JsonProperty("completion_tokens") int completionTokens,
        @JsonProperty("total_tokens") int totalTokens
    ){}

}
