package com.coherentsolutions.restful.UserEndpoints;

import com.coherentsolutions.restful.*;
import com.coherentsolutions.restful.auth.AuthenticationStrategy;
import com.coherentsolutions.restful.auth.BearerTokenAuthentication;
import com.coherentsolutions.restful.auth.OAuth2Client;
import com.coherentsolutions.restful.user.User;
import com.coherentsolutions.restful.user.UserService;
import io.qameta.allure.*;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Epic("User Management")
@Feature("Concurrent User Updates")
public class UpdateUserConcurrencyTests {

    private static final Logger logger = LoggerFactory.getLogger(UpdateUserConcurrencyTests.class);
    private static UserService userService;
    private static OAuth2Client client;

    // A static variable to hold the user ID
    private static Long testUserId;

    @BeforeAll
    @Step("Global setup: Initializing services and creating a test user")
    static void setUp() throws IOException {
        client = OAuth2Client.getInstance();
        AuthenticationStrategy authStrategy = new BearerTokenAuthentication(client);
        userService = new UserService(authStrategy);

        // Reset zip codes to a known state
        Allure.step("Resetting zip codes to default values");
        client.resetZipCodes(Arrays.asList("10001", "20002", "30003"));

        // Create a user to update
        Allure.step("Creating a test user: ConcurrentUser");
        User user = User.builder()
                .name("ConcurrentUser")
                .email("concurrent@example.com")
                .sex("Male")
                .age(25)
                .zipCode("10001")
                .build();

        ApiResponse response = userService.createUser(user);
        attachResponseDetails(response);
        assertEquals(201, response.getStatusCode(), "User creation failed");

        // Parse response body to get user ID
        JSONObject jsonResponse = new JSONObject(response.getResponseBody());
        testUserId = jsonResponse.getLong("id");
    }

    @Test
    @Order(1)
    @Story("Concurrent Updates to a Single User")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Ensure that multiple concurrent updates to the same user are handled correctly by the API.")
    public void testConcurrentUpdates() throws InterruptedException {
        logger.info("Running Scenario #1: Concurrent updates to a single user");

        ExecutorService executorService = Executors.newFixedThreadPool(5);
        List<Callable<ApiResponse>> tasks = new ArrayList<>();

        for (int i = 0; i < 5; i++) {
            final int index = i;
            tasks.add(() -> {
                User updatedUser = User.builder()
                        .name("UpdatedUser" + index)
                        .email("updated" + index + "@example.com")
                        .sex("Male")
                        .age(30 + index)
                        .zipCode("10001")
                        .build();

                // Start Allure step within each task
                Allure.step("Sending PUT request to update user: " + testUserId + " with data: " + updatedUser);

                return userService.updateUser(testUserId, updatedUser);
            });
        }

        List<Future<ApiResponse>> futures = executorService.invokeAll(tasks);

        for (Future<ApiResponse> future : futures) {
            try {
                ApiResponse response = future.get();
                attachResponseDetails(response);
                assertEquals(200, response.getStatusCode(), "Expected status code 200 for successful update");
            } catch (ExecutionException e) {
                Allure.step("Exception occurred during concurrent update: " + e.getMessage());
                fail("Exception occurred during concurrent update: " + e.getMessage());
            }
        }

        executorService.shutdown();
    }

    // Additional Test Methods can be added here following the same pattern

    /**
     * Attaches response details to the Allure report.
     *
     * @param response The ApiResponse object containing status code and response body.
     */
    @Attachment(value = "Response Status Code", type = "text/plain")
    public static String attachStatusCode(ApiResponse response) {
        return String.valueOf(response.getStatusCode());
    }

    @Attachment(value = "Response Body", type = "text/plain")
    public static String attachResponseBody(ApiResponse response) {
        return response.getResponseBody();
    }

    /**
     * Helper method to attach both status code and response body.
     *
     * @param response The ApiResponse object containing status code and response body.
     */
    private static void attachResponseDetails(ApiResponse response) {
        attachStatusCode(response);
        attachResponseBody(response);
    }
}
