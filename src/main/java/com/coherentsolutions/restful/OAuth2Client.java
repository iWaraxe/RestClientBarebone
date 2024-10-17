package com.coherentsolutions.restful;

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

public class OAuth2Client {
    private static final Logger logger = LoggerFactory.getLogger(OAuth2Client.class);
    private static final String TOKEN_URL = "http://localhost:8080/oauth2/token";
    private static final String API_BASE_URL = "http://localhost:8080/api";
    private static final String CLIENT_ID = "0oa157tvtugfFXEhU4x7";
    private static final String CLIENT_SECRET = "X7eBCXqlFC7x-mjxG5H91IRv_Bqe1oq7ZwXNA8aq";

    private static OAuth2Client instance;
    private String readToken;
    private String writeToken;

    private OAuth2Client() {
        // Private constructor to prevent instantiation
    }

    public static synchronized OAuth2Client getInstance() {
        if (instance == null) {
            instance = new OAuth2Client();
        }
        return instance;
    }

    public String getReadToken() throws IOException {
        if (readToken == null) {
            readToken = getToken("read");
        }
        return readToken;
    }

    public String getWriteToken() throws IOException {
        if (writeToken == null) {
            writeToken = getToken("write");
        }
        return writeToken;
    }

    private String getToken(String scope) throws IOException {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(TOKEN_URL);
            String auth = CLIENT_ID + ":" + CLIENT_SECRET;
            String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
            httpPost.setHeader("Authorization", "Basic " + encodedAuth);
            httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded");

            String body = "grant_type=client_credentials&scope=" + scope;
            httpPost.setEntity(new StringEntity(body));

            return httpClient.execute(httpPost, response -> {
                String responseBody = new String(response.getEntity().getContent().readAllBytes(), StandardCharsets.UTF_8);
                JSONObject jsonResponse = new JSONObject(responseBody);
                return jsonResponse.getString("access_token");
            });
        }
    }

    public List<String> getAvailableZipCodes() throws IOException {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet httpGet = new HttpGet(API_BASE_URL + "/zip-codes");
            httpGet.setHeader("Authorization", "Bearer " + getReadToken());

            return httpClient.execute(httpGet, response -> {
                String responseBody = new String(response.getEntity().getContent().readAllBytes(), StandardCharsets.UTF_8);
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
}