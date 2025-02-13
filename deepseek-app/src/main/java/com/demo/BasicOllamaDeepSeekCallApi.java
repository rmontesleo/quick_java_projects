package com.demo;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Hello world!
 *
 */
public class BasicOllamaDeepSeekCallApi {

    //private static final String BASE_URL = "http://localhost:11434";
    private static final String BASE_URL = "http://68.183.103.54:11434";

    public static void main(String[] args) throws IOException, InterruptedException {
        var body = """
                {
                    "model": "deepseek-r1:7b",
                    "stream": false,
                    "messages": [
                        {
                            "role": "user",
                            "content": "Can you explain what GPT and LLM are and how they are related?"
                        }
                    ]
                }
                """;

        var request = HttpRequest.newBuilder()
                .uri(URI.create( BASE_URL + "/api/chat"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        var client = HttpClient.newHttpClient();
        var response = client.send(request, HttpResponse.BodyHandlers.ofString());
        var responseBody = response.body();
        
        System.out.println(responseBody);


        if (!responseBody.isBlank()){
            System.out.println("Response received");
            ObjectMapper mapper = new ObjectMapper();
            Object json = mapper.readValue(responseBody, Object.class);
            String prettyJson = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(json);
            System.out.println(prettyJson);
        } else {
            System.out.println("No response received");
        }

    }
}
