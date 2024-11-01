package com.coherentsolutions.restful.UserEndpoints;

import com.coherentsolutions.restful.*;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class UpdateUserConcurrencyTest {

    private static UserService userService;
    private static OAuth2Client client;

    private static Long testUserId;

    @BeforeAll
    static void setUp() throws IOException {
        client = OAuth2Client.getInstance();
        AuthenticationStrategy authStrategy = new BearerTokenAuthentication(client);
        userService = new UserService(authStrategy);
        // Clean up existing users
        userService.deleteAllUsers();

        // Reset zip codes to a known state
        client.resetZipCodes(Arrays.asList("10001", "20002", "30003"));

        // Create a user to update
        User user = User.builder()
                .name("ConcurrentUser")
                .email("concurrent@example.com")
                .sex("Male")
                .age(25)
                .zipCode("10001")
                .build();

        ApiResponse response = userService.createUser(user);
        assertEquals(201, response.getStatusCode(), "User creation failed");

        // Parse response body to get user ID
        JSONObject jsonResponse = new JSONObject(response.getResponseBody());
        testUserId = jsonResponse.getLong("id");
    }

    @Test
    public void testConcurrentUpdates() throws InterruptedException {
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

                return userService.updateUser(testUserId, updatedUser);
            });
        }

        List<Future<ApiResponse>> futures = executorService.invokeAll(tasks);

        for (Future<ApiResponse> future : futures) {
            try {
                ApiResponse response = future.get();
                assertEquals(200, response.getStatusCode(), "Update failed");
            } catch (ExecutionException e) {
                fail("Exception occurred during concurrent update: " + e.getMessage());
            }
        }

        executorService.shutdown();
    }
}
