package com.dropBox.test.Controller;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

import com.dropBox.test.service.DropBoxService;

@RestController
@RequestMapping("/api")
public class DropBoxController {

    private static final Logger logger = LoggerFactory.getLogger(DropBoxController.class);

    @Autowired
    private DropBoxService dropboxService;

    /**
     * Initiate Dropbox OAuth authorization
     */
    @GetMapping("/authorize/dropbox")
    public RedirectView authorizeDropbox() {
        try {
            logger.info("Generating Dropbox authorization URL manually");
            String authUrl = dropboxService.generateAuthorizationUrl();
            logger.debug("Authorization URL: {}", authUrl);

            return new RedirectView(authUrl);
        } catch (Exception e) {
            logger.error("Error generating authorization URL", e);
            throw new RuntimeException("Authorization failed", e);
        }
    }

    /**
     * Handle Dropbox OAuth redirect
     */
    @GetMapping("/dropbox/redirect")
    public ResponseEntity<Map<String, Object>> handleDropboxRedirect(
            @RequestParam String code,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) String error) {

        try {
            if (error != null) {
                logger.error("OAuth error: {}", error);
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", error);
                return ResponseEntity.badRequest().body(errorResponse);
            }

            logger.info("Handling OAuth redirect with code: {}", code);

            // Exchange code for token using manual HTTP call
            Map<String, Object> tokenData = dropboxService.exchangeCodeForToken(code);

            // Get team info using manual API call
            String accessToken = (String) tokenData.get("access_token");
            String teamInfo = dropboxService.getTeamInfo(accessToken);

            Map<String, Object> response = new HashMap<>();
            response.put("team_info", teamInfo);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error handling OAuth redirect", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to complete OAuth flow");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * Refresh access token endpoint
     */
    @PostMapping("/dropbox/refresh-token")
    public ResponseEntity<Map<String, Object>> refreshToken(@RequestBody Map<String, String> request) {
        try {
            String refreshToken = request.get("refresh_token");
            if (refreshToken == null || refreshToken.trim().isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "Refresh token is required");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            Map<String, Object> newTokenData = dropboxService.refreshAccessToken(refreshToken);
            return ResponseEntity.ok(newTokenData);

        } catch (Exception e) {
            logger.error("Error refreshing token", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to refresh token");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "OK");
        response.put("service", "Dropbox Manual OAuth Spring Boot");
        return ResponseEntity.ok(response);
    }
}
