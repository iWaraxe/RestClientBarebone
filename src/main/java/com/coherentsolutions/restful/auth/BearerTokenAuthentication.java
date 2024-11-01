package com.coherentsolutions.restful.auth;

import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase;

import java.io.IOException;

// BearerTokenAuthentication.java
public class BearerTokenAuthentication implements AuthenticationStrategy {
    private final OAuth2Client authClient;

    public BearerTokenAuthentication(OAuth2Client authClient) {
        this.authClient = authClient;
    }

    @Override
    public void authenticate(HttpUriRequestBase request) throws IOException {
        String token = authClient.getWriteToken();
        if (token == null || token.isEmpty()) {
            request.setHeader("Authorization", "Bearer invalid_token");
        } else {
            request.setHeader("Authorization", "Bearer " + token);
        }
    }
}
