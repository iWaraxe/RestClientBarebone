package com.coherentsolutions.restful.auth;

import com.coherentsolutions.restful.exception.AuthenticationException;
import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class BearerTokenAuthentication implements AuthenticationStrategy {
    private static final Logger logger = LoggerFactory.getLogger(BearerTokenAuthentication.class);
    private final OAuth2Client authClient;

    public BearerTokenAuthentication(OAuth2Client authClient) {
        this.authClient = authClient;
    }

    @Override
    public void authenticate(HttpUriRequestBase request) throws IOException {
        try {
            String method = request.getMethod().toUpperCase();
            String token;

            // Select appropriate token based on method
            if (method.equals("PUT") || method.equals("POST") ||
                    method.equals("DELETE") || method.equals("PATCH")) {
                token = authClient.getWriteToken();
                logger.debug("Using write token for {} request", method);
            } else {
                token = authClient.getReadToken();
                logger.debug("Using read token for {} request", method);
            }

            // Token validation
            if (token == null) {
                logger.warn("Authentication failed: No token available");
                throw new AuthenticationException("No token available");
            }

            if ("invalid_token".equals(token)) {
                logger.warn("Authentication failed: Invalid token");
                throw new AuthenticationException("Invalid token");
            }

            logger.debug("Setting Authorization header with token");
            request.setHeader("Authorization", "Bearer " + token);
        } catch (IOException e) {
            logger.error("Authentication error: {}", e.getMessage());
            throw new AuthenticationException(e.getMessage());
        }
    }

    private String getAppropriateToken(HttpUriRequestBase request) throws IOException {
        String method = request.getMethod().toUpperCase();
        // For unsafe methods (PUT, POST, DELETE), use write token
        if (method.equals("PUT") || method.equals("POST") || method.equals("DELETE") || method.equals("PATCH")) {
            String token = authClient.getWriteToken();
            logger.debug("Got write token: {}", token);
            return token;
        } else {
            // For safe methods (GET, HEAD), use read token
            String token = authClient.getReadToken();
            logger.debug("Got read token: {}", token);
            return token;
        }
    }
}