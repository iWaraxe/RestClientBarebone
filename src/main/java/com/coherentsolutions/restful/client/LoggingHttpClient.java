package com.coherentsolutions.restful.client;

import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;

public class LoggingHttpClient implements HttpClientComponent {

    private static final Logger logger = LoggerFactory.getLogger(LoggingHttpClient.class);
    private final HttpClientComponent wrappedClient;

    public LoggingHttpClient(HttpClientComponent wrappedClient) {
        this.wrappedClient = wrappedClient;
    }

    @Override
    public CloseableHttpResponse execute(HttpUriRequestBase request) throws IOException {
        try {
            logger.info("Executing Request: {} {}", request.getMethod(), request.getUri());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        CloseableHttpResponse response = wrappedClient.execute(request);
        logger.info("Received Response: Status Code {}", response.getCode());
        return response;
    }
}
