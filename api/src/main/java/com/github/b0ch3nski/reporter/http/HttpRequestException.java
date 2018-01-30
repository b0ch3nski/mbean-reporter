package com.github.b0ch3nski.reporter.http;

import java.io.IOException;

public class HttpRequestException extends IOException {
    private static final long serialVersionUID = -463380245795810585L;
    private final String response;

    public HttpRequestException(String message, String response) {
        super(message);
        this.response = response;
    }

    public String getResponse() {
        return response;
    }
}
