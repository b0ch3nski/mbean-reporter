package com.github.b0ch3nski.reporter.persistence;

import com.github.b0ch3nski.reporter.http.HttpRequest;
import com.github.b0ch3nski.reporter.model.Measurement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class InfluxDB implements MetricsDatabase {
    private static final Logger LOG = LoggerFactory.getLogger(InfluxDB.class);
    private final String influxUrl;
    private final String dbName;

    public InfluxDB(String influxUrl, String dbName) {
        this.influxUrl = influxUrl;
        this.dbName = dbName;
    }

    @Override
    public void createDatabase() {
        try {
            HttpRequest
                    .post(influxUrl + "/query")
                    .withPayload(("q=CREATE DATABASE " + dbName))
                    .expectCode(200);
        } catch (IOException e) {
            LOG.warn("Failed to create InfluxDB database name={}, cause={}", dbName, e.getMessage());
        }
    }

    private static String convert(Measurement measurement) {
        return String.format("%s,host=TEST value=%s", measurement.getName(), measurement.getValue());
    }

    @Override
    public void sendMeasurements(Stream<Measurement> measurements) {
        String payload = measurements
                .map(InfluxDB::convert)
                .collect(Collectors.joining("\n"));

        try {
            HttpRequest
                    .post(influxUrl + "/write?db=" + dbName)
                    .withPayload(payload)
                    .expectCode(204);
        } catch (IOException e) {
            LOG.warn("Failed to send measurements to InfluxDB, cause={}", e.getMessage());
        }
    }
}
