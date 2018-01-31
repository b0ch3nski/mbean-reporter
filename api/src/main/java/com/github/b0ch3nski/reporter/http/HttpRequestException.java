package com.github.b0ch3nski.reporter.http;

import java.io.IOException;

/**
 * Signals that HTTP request has failed. Keeps additional details (server response).
 *
 * @author Piotr Bochenski
 */
public class HttpRequestException extends IOException {
    private static final long serialVersionUID = -463380245795810585L;
    private final String response;

    /**
     * @param message  short explanation of what has gone wrong
     * @param response captured server response for failed request
     */
    public HttpRequestException(String message, String response) {
        super(message);
        this.response = response;
    }

    /**
     * @return captured server response for failed request
     */
    public String getResponse() {
        return response;
    }
}
