package com.github.b0ch3nski.reporter.persistence;

import com.github.b0ch3nski.reporter.model.Measurement;

import java.util.stream.Stream;

/**
 * Definition of how to handle the database that provides persistence for metric data.
 *
 * @author Piotr Bochenski
 */
public interface MetricsDatabase {
    /**
     * Creates appropriate structure, e.g. database, table, schema, etc. that will be used for metrics storage.
     *
     * @throws MetricsDatabaseException when failed
     */
    void createDatabase() throws MetricsDatabaseException;

    /**
     * Sends metrics to database.
     *
     * @throws MetricsDatabaseException when failed
     */
    void sendMeasurements(Stream<Measurement> measurements) throws MetricsDatabaseException;
}
