package com.coherentsolutions.restful.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ApplicationConfig {
    private static ApplicationConfig instance;
    private final Properties properties;

    private ApplicationConfig() {
        properties = new Properties();
        loadProperties();
    }

    public static synchronized ApplicationConfig getInstance() {
        if (instance == null) {
            instance = new ApplicationConfig();
        }
        return instance;
    }

    private void loadProperties() {
        String env = System.getProperty("env", "dev");
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("application-" + env + ".properties")) {
            if (input == null) {
                throw new RuntimeException("Unable to find application-" + env + ".properties");
            }
            properties.load(input);
        } catch (IOException e) {
            throw new RuntimeException("Error loading configuration", e);
        }
    }

    public String getApiBaseUrl() {
        return properties.getProperty("api.base.url");
    }

    public String getTokenUrl() {
        return properties.getProperty("oauth2.token.url");
    }

    public String getClientId() {
        return properties.getProperty("oauth2.client.id");
    }

    public String getClientSecret() {
        return properties.getProperty("oauth2.client.secret");
    }

    public int getMaxRetries() {
        return Integer.parseInt(properties.getProperty("http.max.retries", "3"));
    }

    public long getTokenExpiryBuffer() {
        return Long.parseLong(properties.getProperty("oauth2.token.expiry.buffer", "300000"));
    }
}