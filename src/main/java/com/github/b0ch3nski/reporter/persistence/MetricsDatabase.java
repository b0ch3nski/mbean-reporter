package com.github.b0ch3nski.reporter.persistence;

import com.github.b0ch3nski.reporter.model.Measurement;

import java.util.stream.Stream;

public interface MetricsDatabase {

    MetricsDatabase setDbUrl(String dbUrl);

    MetricsDatabase setDbName(String dbName);

    void createDatabase();

    void sendMeasurements(Stream<Measurement> measurements);
}
