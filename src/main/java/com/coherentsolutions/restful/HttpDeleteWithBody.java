package com.coherentsolutions.restful;

import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase;

import java.net.URI;

public class HttpDeleteWithBody extends HttpUriRequestBase {
    public static final String METHOD_NAME = "DELETE";

    public HttpDeleteWithBody(final String uri) {
        super(METHOD_NAME, URI.create(uri));
    }

    public HttpDeleteWithBody(final URI uri) {
        super(METHOD_NAME, uri);
    }
}
