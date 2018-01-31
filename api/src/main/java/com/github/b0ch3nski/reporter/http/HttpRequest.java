package com.github.b0ch3nski.reporter.http;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

/**
 * Wraps Java {@code HttpURLConnection} API to make it more usable for modern web-related workloads.
 *
 * @author Piotr Bochenski
 */
public final class HttpRequest {
    private static final Charset CHARSET = StandardCharsets.UTF_8;
    private static final int TIMEOUT_MILLIS = 2000;
    private final HttpURLConnection connection;

    private HttpRequest(String url, String method) throws IOException {
        connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestProperty("Accept-Charset", CHARSET.name());
        connection.setRequestMethod(method);
        connection.setConnectTimeout(TIMEOUT_MILLIS);
        connection.setReadTimeout(TIMEOUT_MILLIS);
    }

    /**
     * Produces {@code HttpRequest} for GET method.
     *
     * @param url destination address of the request
     */
    public static HttpRequest get(String url) throws IOException {
        return new HttpRequest(url, "GET");
    }

    /**
     * Produces {@code HttpRequest} for POST method.
     *
     * @param url destination address of the request
     */
    public static HttpRequest post(String url) throws IOException {
        return new HttpRequest(url, "POST");
    }

    /**
     * Fills request content with specified payload.
     *
     * @param payload content to be sent
     */
    public HttpRequest withPayload(String payload) throws IOException {
        byte[] payloadBytes = payload.getBytes(CHARSET);

        connection.setRequestProperty("Content-Length", String.valueOf(payloadBytes.length));
        connection.setDoOutput(true);

        try (OutputStream writer = new BufferedOutputStream(connection.getOutputStream())) {
            writer.write(payloadBytes);
        }
        return this;
    }

    /**
     * @return HTTP return code for executed request
     */
    public int getCode() throws IOException {
        return connection.getResponseCode();
    }

    private String getResponse(int code) throws IOException {
        try (Scanner scanner = new Scanner(
                new BufferedInputStream(
                        ((code >= 200) && (code < 300)) ? connection.getInputStream() : connection.getErrorStream()
                ), CHARSET.name())
        ) {
            return scanner.next();
        }
    }

    /**
     * @return server response for executed request
     */
    public String getResponse() throws IOException {
        return getResponse(getCode());
    }

    /**
     * Ensures that request return code matches expected code.
     *
     * @param expected code that server has to return
     * @throws HttpRequestException when returned code didn't match expected code
     */
    public void expectCode(int expected) throws HttpRequestException, IOException {
        int actual = getCode();

        if (actual != expected)
            throw new HttpRequestException(
                    String.format("Got HTTP return code=%d but expected was=%d", actual, expected),
                    getResponse(actual)
            );
    }
}
