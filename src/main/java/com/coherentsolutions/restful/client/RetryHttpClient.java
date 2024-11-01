package com.coherentsolutions.restful.client;

import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;

public class RetryHttpClient implements HttpClientComponent {

    private static final Logger logger = LoggerFactory.getLogger(RetryHttpClient.class);
    private final HttpClientComponent wrappedClient;
    private final int maxRetries;

    public RetryHttpClient(HttpClientComponent wrappedClient, int maxRetries) {
        this.wrappedClient = wrappedClient;
        this.maxRetries = maxRetries;
    }

    @Override
    public CloseableHttpResponse execute(HttpUriRequestBase request) throws IOException {
        int attempt = 0;
        IOException lastException = null;

        while (attempt < maxRetries) {
            try {
                logger.info("Attempt {} for Request: {} {}", attempt + 1, request.getMethod(), request.getUri());
                CloseableHttpResponse response = wrappedClient.execute(request);
                if (response.getCode() >= 200 && response.getCode() < 500) {
                    // Do not retry for client errors (4xx)
                    return response;
                }
            } catch (IOException e) {
                lastException = e;
                try {
                    logger.warn("Attempt {} failed for Request: {} {}. Retrying...", attempt + 1, request.getMethod(), request.getUri());
                } catch (URISyntaxException ex) {
                    throw new RuntimeException(ex);
                }
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
            attempt++;
        }

        try {
            logger.error("All {} attempts failed for Request: {} {}", maxRetries, request.getMethod(), request.getUri());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        if (lastException != null) {
            throw lastException;
        } else {
            throw new IOException("Failed to execute request after " + maxRetries + " attempts");
        }
    }
}
