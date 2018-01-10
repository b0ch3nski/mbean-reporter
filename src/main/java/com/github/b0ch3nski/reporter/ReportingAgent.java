package com.github.b0ch3nski.reporter;

import com.github.b0ch3nski.reporter.persistence.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.instrument.Instrumentation;
import java.lang.management.ManagementFactory;
import java.util.Optional;
import java.util.concurrent.*;

public final class ReportingAgent {
    private static final Logger LOG = LoggerFactory.getLogger(ReportingAgent.class);
    private static final ScheduledExecutorService EXECUTOR = Executors.newSingleThreadScheduledExecutor();
    private static final MetricsDatabase METRICS_DB;

    static {
        // TODO: implement configuration handling
        Class<?> dbImpl = InfluxDB.class;
        String dbUrl = "http://localhost:8086";
        String dbName = "test";

        Optional<MetricsDatabase> database = MetricsDatabaseService.getInstance().getDatabase(dbImpl);
        if (!database.isPresent())
            LOG.warn("Unable to find 'MetricsDatabase' implementation={}", dbImpl.getSimpleName());

        METRICS_DB = database.get()
                .setDbUrl(dbUrl)
                .setDbName(dbName);
    }

    private ReportingAgent() { }

    private static void send() {
        METRICS_DB.sendMeasurements(
                MBeanProcessingHelper.getAllMBeansAsMeasurements()
        );
    }

    public static void premain(String args, Instrumentation inst) {
        LOG.info("mbean-reporter attached to JVM={}", ManagementFactory.getRuntimeMXBean().getName());

        METRICS_DB.createDatabase();
        EXECUTOR.scheduleAtFixedRate(ReportingAgent::send, 0, 10, TimeUnit.SECONDS);
    }
}
