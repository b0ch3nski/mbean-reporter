package com.github.b0ch3nski.reporter.persistence;

import com.github.b0ch3nski.reporter.http.HttpRequest;
import com.github.b0ch3nski.reporter.model.Measurement;
import com.github.b0ch3nski.reporter.services.ConfigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class InfluxDB implements MetricsDatabase {
    private static final Logger LOG = LoggerFactory.getLogger(InfluxDB.class);
    private final String dbUrl;
    private final String dbName;

    public InfluxDB() {
        ConfigService config = ConfigService.getInstance();
        dbUrl = config.getValue("dbUrl", "http://localhost:8086");
        dbName = config.getValue("dbName", "jvm-metrics");
    }

    @Override
    public void createDatabase() {
        try {
            HttpRequest
                    .post(String.format("%s/query", dbUrl))
                    .withPayload(String.format("q=CREATE DATABASE \"%s\"", dbName))
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

        LOG.trace("Sending payload to InfluxDB:\n{}", payload);

        try {
            HttpRequest
                    .post(String.format("%s/write?db=%s", dbUrl, dbName))
                    .withPayload(payload)
                    .expectCode(204);
        } catch (IOException e) {
            LOG.warn("Failed to send measurements to InfluxDB, cause={}", e.getMessage());
        }
    }
}
