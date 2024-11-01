package com.coherentsolutions.restful.user;

import com.coherentsolutions.restful.ApiResponse;
import com.coherentsolutions.restful.client.BasicHttpClient;
import com.coherentsolutions.restful.HttpDeleteWithBody;
import com.coherentsolutions.restful.auth.AuthenticationStrategy;
import com.coherentsolutions.restful.client.HttpClientComponent;
import com.coherentsolutions.restful.client.LoggingHttpClient;
import com.coherentsolutions.restful.client.RetryHttpClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.hc.client5.http.classic.methods.*;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.net.URIBuilder;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    private static final String API_BASE_URL = "http://localhost:8080/api";
    private final AuthenticationStrategy authStrategy;
    private final HttpClientComponent httpClient;
    private ObjectMapper objectMapper;


    public UserService(AuthenticationStrategy authStrategy) {
        // Initialize the basic client
        HttpClientComponent basicClient = new BasicHttpClient();

        // Apply decorators
        HttpClientComponent loggingClient = new LoggingHttpClient(basicClient);
        HttpClientComponent retryClient = new RetryHttpClient(loggingClient, 3);

        this.authStrategy = authStrategy;
        this.httpClient = retryClient;
        this.objectMapper = new ObjectMapper();
    }

    public ApiResponse executeRequest(HttpUriRequestBase request) throws IOException {
        authStrategy.authenticate(request);
        try {
            logger.debug("Executing request: {} {}", request.getMethod(), request.getUri());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

        try (CloseableHttpResponse response = httpClient.execute(request)) {
            int statusCode = response.getCode();
            String responseBody = "";

            if (response.getEntity() != null) {
                responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
            }

            logger.debug("Received response - Status Code: {}", statusCode);
            logger.debug("Received response - Body: {}", responseBody);

            return new ApiResponse(statusCode, responseBody);
        } catch (ParseException e) {
            logger.error("Error parsing response", e);
            throw new RuntimeException(e);
        }
    }

    public ApiResponse getUsers(Map<String, String> queryParams) throws IOException {
        logger.info("Fetching users with query parameters: {}", queryParams);
        try {
            URIBuilder uriBuilder = new URIBuilder(API_BASE_URL + "/users");
            if (queryParams != null) {
                queryParams.forEach(uriBuilder::addParameter);
            }
            HttpGet httpGet = new HttpGet(uriBuilder.build());

            return executeRequest(httpGet);
        } catch (URISyntaxException e) {
            logger.error("Invalid URI syntax", e);
            throw new RuntimeException(e);
        }
    }

    public ApiResponse createUser(User user) throws IOException {
        logger.info("Creating user: {}", user.getName());
        HttpPost httpPost = new HttpPost(API_BASE_URL + "/users");
        httpPost.setHeader("Content-Type", "application/json");

        // Construct the JSON payload
        JSONObject json = new JSONObject();
        json.put("name", user.getName());
        json.put("email", user.getEmail());
        json.put("sex", user.getSex());
        json.put("age", user.getAge());

        if (user.getZipCode() != null && !user.getZipCode().isEmpty()) {
            JSONObject zipCodeJson = new JSONObject();
            zipCodeJson.put("code", user.getZipCode());
            json.put("zipCode", zipCodeJson);
        }

        logger.info("Sending POST request to create user with payload: {}", json.toString());
        httpPost.setEntity(new StringEntity(json.toString(), StandardCharsets.UTF_8));

        return executeRequest(httpPost);
    }

    public ApiResponse updateUser(Long id, User user) throws IOException {
        HttpPut httpPut = new HttpPut(API_BASE_URL + "/users/" + id);
        httpPut.setHeader("Content-Type", "application/json");

        JSONObject json = new JSONObject();
        json.put("name", user.getName());
        json.put("email", user.getEmail());
        json.put("sex", user.getSex());
        json.put("age", user.getAge());

        if (user.getZipCode() != null && !user.getZipCode().isEmpty()) {
            JSONObject zipCodeJson = new JSONObject();
            zipCodeJson.put("code", user.getZipCode());
            json.put("zipCode", zipCodeJson);
        }

        httpPut.setEntity(new StringEntity(json.toString(), StandardCharsets.UTF_8));

        return executeRequest(httpPut);
    }

    public ApiResponse updateUser(UpdateUserDto updateUserDto) throws IOException {
        HttpPut httpPut = new HttpPut(API_BASE_URL + "/users");
        httpPut.setHeader("Content-Type", "application/json");

        // Construct JSON payload
        JSONObject json = new JSONObject();
        JSONObject userNewValuesJson = new JSONObject();
        JSONObject userToChangeJson = new JSONObject();

        // userNewValues
        UserDto userNewValues = updateUserDto.getUserNewValues();
        if (userNewValues.getName() != null) {
            userNewValuesJson.put("name", userNewValues.getName());
        }
        if (userNewValues.getEmail() != null) {
            userNewValuesJson.put("email", userNewValues.getEmail());
        }
        if (userNewValues.getSex() != null) {
            userNewValuesJson.put("sex", userNewValues.getSex());
        }
        if (userNewValues.getAge() != null) {
            userNewValuesJson.put("age", userNewValues.getAge());
        }
        if (userNewValues.getZipCode() != null) {
            userNewValuesJson.put("zipCode", userNewValues.getZipCode());
        }

        // userToChange
        UserDto userToChange = updateUserDto.getUserToChange();
        if (userToChange.getName() != null) {
            userToChangeJson.put("name", userToChange.getName());
        }
        if (userToChange.getEmail() != null) {
            userToChangeJson.put("email", userToChange.getEmail());
        }
        if (userToChange.getSex() != null) {
            userToChangeJson.put("sex", userToChange.getSex());
        }
        if (userToChange.getAge() != null) {
            userToChangeJson.put("age", userToChange.getAge());
        }
        if (userToChange.getZipCode() != null) {
            userToChangeJson.put("zipCode", userToChange.getZipCode());
        }

        json.put("userNewValues", userNewValuesJson);
        json.put("userToChange", userToChangeJson);

        httpPut.setEntity(new StringEntity(json.toString(), StandardCharsets.UTF_8));

        return executeRequest(httpPut);
    }

    public ApiResponse partialUpdateUser(Long id, Map<String, Object> updates) throws IOException {
        HttpPatch httpPatch = new HttpPatch(API_BASE_URL + "/users/" + id);
        httpPatch.setHeader("Content-Type", "application/json");

        JSONObject json = new JSONObject(updates);
        httpPatch.setEntity(new StringEntity(json.toString(), StandardCharsets.UTF_8));

        return executeRequest(httpPatch);
    }

    public ApiResponse deleteUser(UserDto userDto) throws IOException {
        logger.info("Deleting user: {}", userDto.getName());

        HttpDeleteWithBody httpDelete = new HttpDeleteWithBody(API_BASE_URL + "/users");
        httpDelete.setHeader("Content-Type", "application/json");

        // Convert UserDto to JSON
        JSONObject json = new JSONObject();
        json.put("name", userDto.getName());
        json.put("sex", userDto.getSex());
        if (userDto.getEmail() != null) {
            json.put("email", userDto.getEmail());
        }
        if (userDto.getAge() != null) {
            json.put("age", userDto.getAge());
        }
        if (userDto.getZipCode() != null) {
            json.put("zipCode", userDto.getZipCode());
        }

        httpDelete.setEntity(new StringEntity(json.toString(), StandardCharsets.UTF_8));

        return executeRequest(httpDelete);
    }

    public void deleteAllUsers() throws IOException {
        logger.info("Deleting all users");
        HttpDelete httpDelete = new HttpDelete(API_BASE_URL + "/users/all");
        ApiResponse response = executeRequest(httpDelete);
        int statusCode = response.getStatusCode();
        if (statusCode != 204) {
            throw new IOException("Failed to delete all users. Status code: " + statusCode + ", Response: " + response.getResponseBody());
        }
    }

    public ApiResponse sendInvalidDeleteMethodRequest() throws IOException {
        logger.info("Sending invalid HTTP method request to /users/available endpoint");

        // Using HttpDelete on an endpoint that doesn't support DELETE
        HttpDelete httpDelete = new HttpDelete(API_BASE_URL + "/users/available");

        return executeRequest(httpDelete);
    }


    public ApiResponse sendInvalidMethodRequest() throws IOException {
        logger.info("Sending invalid HTTP method request to /users endpoint");

        // Using HttpPatch as an invalid method for POST-only endpoint
        HttpPatch httpPatch = new HttpPatch(API_BASE_URL + "/users");

        return executeRequest(httpPatch);
    }

    public ApiResponse uploadUsers(File jsonFile) throws IOException {
        HttpPost httpPost = new HttpPost(API_BASE_URL + "/users/upload");

        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.addBinaryBody("file", jsonFile, ContentType.APPLICATION_JSON, jsonFile.getName());

        HttpEntity multipart = builder.build();
        httpPost.setEntity(multipart);

        return executeRequest(httpPost);
    }
}
