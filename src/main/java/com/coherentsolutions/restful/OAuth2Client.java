package com.coherentsolutions.restful;

import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class OAuth2Client {
    private static final String TOKEN_URL = "http://localhost:8080/oauth2/token";
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
}