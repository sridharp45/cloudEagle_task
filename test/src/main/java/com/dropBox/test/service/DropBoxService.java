
package com.dropBox.test.service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class DropBoxService {

    @Value("${dropbox.client.id}")
    private String clientId;

    @Value("${dropbox.client.secret}")
    private String clientSecret;

    @Value("${dropbox.redirect.uri}")
    private String redirectUri;

    @Value("${dropbox.auth.url}")
    private String authUrl;

    @Value("${dropbox.token.url}")
    private String tokenUrl;

    @Value("${dropbox.api.url}")
    private String apiUrl;

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    public DropBoxService() {
        this.webClient = WebClient.builder().build();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Generate authorization URL manually (equivalent to Node.js authorize function)
     */
    public String generateAuthorizationUrl() {
        try {
            String state = UUID.randomUUID().toString();

            // Construct authorization URL manually
            StringBuilder authUrlBuilder = new StringBuilder(authUrl);
            authUrlBuilder.append("?client_id=").append(URLEncoder.encode(clientId, StandardCharsets.UTF_8));
            authUrlBuilder.append("&redirect_uri=").append(URLEncoder.encode(redirectUri, StandardCharsets.UTF_8));
            authUrlBuilder.append("&response_type=code");
            authUrlBuilder.append("&state=").append(state);
            authUrlBuilder.append("&token_access_type=offline"); // For refresh tokens


            return authUrlBuilder.toString();

        } catch (Exception e) {
            throw new RuntimeException("Failed to generate authorization URL", e);
        }
    }

    /**
     * Exchange authorization code for access token using manual HTTP calls
     */
    public Map<String, Object> exchangeCodeForToken(String code) {
        try {
            // Prepare form data for token exchange
            MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
            formData.add("code", code);
            formData.add("grant_type", "authorization_code");
            formData.add("client_id", clientId);
            formData.add("client_secret", clientSecret);
            formData.add("redirect_uri", redirectUri);

            // Make POST request to token endpoint
            String response = webClient.post()
                    .uri(tokenUrl)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                    .body(BodyInserters.fromFormData(formData))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            // Parse JSON response
            JsonNode jsonNode = objectMapper.readTree(response);

            Map<String, Object> tokenData = new HashMap<>();
            tokenData.put("access_token", jsonNode.get("access_token").asText());
            tokenData.put("token_type", jsonNode.get("token_type").asText());

            // Handle refresh token if present (offline access)
            if (jsonNode.has("refresh_token")) {
                tokenData.put("refresh_token", jsonNode.get("refresh_token").asText());
            }

            if (jsonNode.has("expires_in")) {
                tokenData.put("expires_in", jsonNode.get("expires_in").asInt());
            }

            return tokenData;

        } catch (Exception e) {
            throw new RuntimeException("Failed to exchange code for token", e);
        }
    }

    /**
     * Get user account info using manual API call
     */
public String getTeamInfo(String accessToken) {
    try {
        String response = WebClient.create("https://api.dropboxapi.com")
                .post()
                .uri("/2/team/get_info")  
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        return response;

    } catch (Exception e) {
        throw new RuntimeException("Failed to get user account info", e);
    }
}

    /**
     * Refresh access token using refresh token
     */
    public Map<String, Object> refreshAccessToken(String refreshToken) {
        try {
            MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
            formData.add("grant_type", "refresh_token");
            formData.add("refresh_token", refreshToken);
            formData.add("client_id", clientId);
            formData.add("client_secret", clientSecret);

            String response = webClient.post()
                    .uri(tokenUrl)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                    .body(BodyInserters.fromFormData(formData))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            JsonNode jsonNode = objectMapper.readTree(response);

            Map<String, Object> tokenData = new HashMap<>();
            tokenData.put("access_token", jsonNode.get("access_token").asText());
            tokenData.put("token_type", jsonNode.get("token_type").asText());
            tokenData.put("expires_in", jsonNode.get("expires_in").asInt());

            return tokenData;

        } catch (Exception e) {
            throw new RuntimeException("Failed to refresh access token", e);
        }
    }
}
