package com.github.b0ch3nski.reporter.persistence;

/**
 * Signals that action related to {@code MetricsDatabase} has failed.
 *
 * @author Piotr Bochenski
 */
public class MetricsDatabaseException extends Exception {
    private static final long serialVersionUID = 2872237700813117690L;

    public MetricsDatabaseException(String message, Throwable cause) {
        super(message, cause);
    }
}
