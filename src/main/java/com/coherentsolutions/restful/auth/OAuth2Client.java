package com.coherentsolutions.restful.auth;

import com.coherentsolutions.restful.config.ApplicationConfig;
import com.coherentsolutions.restful.exception.AuthenticationException;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import static com.coherentsolutions.restful.user.UserService.API_BASE_URL;

public class OAuth2Client implements IOAuth2Client {
    private static final Logger logger = LoggerFactory.getLogger(OAuth2Client.class);
    private final ApplicationConfig config;

    private static OAuth2Client instance;
    private String readToken;
    private String writeToken;
    private boolean tokensInvalidated = false;
    private long readTokenExpiry;
    private long writeTokenExpiry;

    private OAuth2Client() {
        this.config = ApplicationConfig.getInstance();
    }

    public static synchronized OAuth2Client getInstance() {
        if (instance == null) {
            instance = new OAuth2Client();
        }
        return instance;
    }

    @Override
    public String getReadToken() throws IOException {
        if (tokensInvalidated) {
            logger.info("Tokens are invalidated");
            throw new AuthenticationException("Tokens have been invalidated");
        }
        if (readToken != null && readToken.equals("invalid_token")) {
            logger.info("Read token is explicitly set to invalid");
            throw new AuthenticationException("Invalid read token");
        }
        if (readToken == null) {
            readToken = getToken("read");
        }
        return readToken;
    }

    @Override
    public String getWriteToken() throws IOException {
        if (tokensInvalidated) {
            logger.info("Tokens are invalidated");
            throw new AuthenticationException("Tokens have been invalidated");
        }
        if (writeToken != null && writeToken.equals("invalid_token")) {
            logger.info("Write token is explicitly set to invalid");
            throw new AuthenticationException("Invalid write token");
        }
        if (writeToken == null) {
            writeToken = getToken("write");
        }
        return writeToken;
    }

    // Rest of the methods remain the same...
    private boolean isTokenExpired(String token, long expiry) {
        if (token == null) {
            return true;
        }
        return System.currentTimeMillis() >= expiry - config.getTokenExpiryBuffer();
    }

    private String getToken(String scope) throws IOException {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(config.getTokenUrl());
            String auth = config.getClientId() + ":" + config.getClientSecret();
            String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
            httpPost.setHeader("Authorization", "Basic " + encodedAuth);
            httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded");

            String body = "grant_type=client_credentials&scope=" + scope;
            httpPost.setEntity(new StringEntity(body));

            return httpClient.execute(httpPost, response -> {
                String responseBody = EntityUtils.toString(response.getEntity());
                JSONObject jsonResponse = new JSONObject(responseBody);
                if (scope.equals("read")) {
                    readTokenExpiry = System.currentTimeMillis() + (jsonResponse.getLong("expires_in") * 1000);
                } else {
                    writeTokenExpiry = System.currentTimeMillis() + (jsonResponse.getLong("expires_in") * 1000);
                }
                return jsonResponse.getString("access_token");
            });
        }
    }

    public List<String> getAvailableZipCodes() throws IOException {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet httpGet = new HttpGet(config.getApiBaseUrl() + "/zip-codes");
            httpGet.setHeader("Authorization", "Bearer " + getReadToken());

            return httpClient.execute(httpGet, response -> {
                String responseBody = EntityUtils.toString(response.getEntity());
                JSONArray jsonArray = new JSONArray(responseBody);

                List<String> zipCodes = new ArrayList<>();
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    zipCodes.add(jsonObject.getString("code"));
                }
                logger.info("Extracted zip codes: {}", zipCodes);
                return zipCodes;
            });
        }
    }

    public void addZipCodes(List<String> zipCodes) throws IOException {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(API_BASE_URL + "/zip-codes/expand");
            httpPost.setHeader("Authorization", "Bearer " + getWriteToken());
            httpPost.setHeader("Content-Type", "application/json");

            JSONArray jsonArray = new JSONArray(zipCodes);
            httpPost.setEntity(new StringEntity(jsonArray.toString()));

            httpClient.execute(httpPost, response -> {
                int statusCode = response.getCode();
                if (statusCode != 201) {
                    throw new IOException("Failed to add zip codes. Status code: " + statusCode);
                }
                return null;
            });
        }
    }

    public void resetZipCodes(List<String> zipCodes) throws IOException {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(API_BASE_URL + "/zip-codes/reset");
            httpPost.setHeader("Authorization", "Bearer " + getWriteToken());
            httpPost.setHeader("Content-Type", "application/json");

            JSONArray jsonArray = new JSONArray(zipCodes);
            String jsonBody = jsonArray.toString();
            logger.info("Sending request to reset zip codes: {}", jsonBody);
            httpPost.setEntity(new StringEntity(jsonBody));

            httpClient.execute(httpPost, response -> {
                int statusCode = response.getCode();
                String responseBody = EntityUtils.toString(response.getEntity());
                logger.info("Reset zip codes response status code: {}", statusCode);
                logger.info("Reset zip codes response body: {}", responseBody);
                if (statusCode != 200) {
                    throw new IOException("Failed to reset zip codes. Status code: " + statusCode + ", Response: " + responseBody);
                }
                return null;
            });
        }
    }

    public void invalidateTokens() {
        this.readToken = null;
        this.writeToken = null;
        this.tokensInvalidated = true;
        logger.info("Tokens have been invalidated");
    }

    public void setInvalidToken() {
        this.readToken = "invalid_token";
        this.writeToken = "invalid_token";
        logger.info("Tokens have been set to invalid");
    }

    public void validateTokens() {
        this.readToken = null;
        this.writeToken = null;
        this.tokensInvalidated = false;
        logger.info("Tokens have been validated");
    }
}