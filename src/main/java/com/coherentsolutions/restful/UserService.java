package com.coherentsolutions.restful;

import org.apache.hc.client5.http.classic.methods.HttpDelete;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    private static final String API_BASE_URL = "http://localhost:8080/api";
    private OAuth2Client authClient = OAuth2Client.getInstance();

    public ApiResponse createUser(User user) throws IOException {
        logger.info("Creating user: {}", user.getName());

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(API_BASE_URL + "/users");
            httpPost.setHeader("Authorization", "Bearer " + authClient.getWriteToken());
            httpPost.setHeader("Content-Type", "application/json");

            // Construct the JSON payload
            JSONObject json = new JSONObject();
            json.put("name", user.getName());
            json.put("email", user.getEmail());
            json.put("sex", user.getSex());

            if (user.getZipCode() != null && !user.getZipCode().isEmpty()) {
                JSONObject zipCodeJson = new JSONObject();
                zipCodeJson.put("code", user.getZipCode());
                json.put("zipCode", zipCodeJson);
            }

            logger.info("Sending POST request to create user with payload: {}", json.toString());
            httpPost.setEntity(new StringEntity(json.toString()));

            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                int statusCode = response.getCode();
                String responseBody = "";
                if (response.getEntity() != null) {
                    responseBody = EntityUtils.toString(response.getEntity());
                }
                logger.info("Received response with status code: {}", statusCode);
                return new ApiResponse(statusCode, responseBody);
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void deleteAllUsers() throws IOException {
        logger.info("Deleting all users");

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpDelete httpDelete = new HttpDelete(API_BASE_URL + "/users");
            httpDelete.setHeader("Authorization", "Bearer " + authClient.getWriteToken());

            try (CloseableHttpResponse response = httpClient.execute(httpDelete)) {
                int statusCode = response.getCode();
                String responseBody = "";
                if (response.getEntity() != null) {
                    responseBody = EntityUtils.toString(response.getEntity());
                }
                logger.info("Received response with status code: {}", statusCode);
                if (statusCode != 204) {  // 204 No Content expected
                    throw new IOException("Failed to delete all users. Status code: " + statusCode + ", Response: " + responseBody);
                }
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public ApiResponse sendInvalidMethodRequest() throws IOException {
        logger.info("Sending invalid HTTP method request to /users endpoint");

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            // Using HttpGet as an invalid method for POST-only endpoint
            HttpGet httpGet = new HttpGet(API_BASE_URL + "/users");
            httpGet.setHeader("Authorization", "Bearer " + authClient.getWriteToken());

            try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
                int statusCode = response.getCode();
                String responseBody = "";
                if (response.getEntity() != null) {
                    responseBody = EntityUtils.toString(response.getEntity());
                }
                logger.info("Received response with status code: {}", statusCode);
                return new ApiResponse(statusCode, responseBody);
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
