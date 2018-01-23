package com.github.b0ch3nski.reporter;

import com.github.b0ch3nski.reporter.mbean.MBeanProcessingHelper;
import com.github.b0ch3nski.reporter.persistence.MetricsDatabase;
import com.github.b0ch3nski.reporter.services.ConfigService;
import com.github.b0ch3nski.reporter.services.MetricsDatabaseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.instrument.Instrumentation;
import java.lang.management.ManagementFactory;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public final class ReportingAgent {
    private static final Logger LOG = LoggerFactory.getLogger(ReportingAgent.class);
    private static final ConfigService CFG = ConfigService.getInstance();
    private static final MetricsDatabase METRICS_DB;

    static {
        String defDbImpl = "com.github.b0ch3nski.reporter.persistence.InfluxDB";
        METRICS_DB =
                MetricsDatabaseService.getInstance().getDatabase(
                        CFG.getValue("dbImpl", defDbImpl),
                        defDbImpl
                );
    }

    private ReportingAgent() { }

    private static void send() {
        METRICS_DB.sendMeasurements(
                MBeanProcessingHelper.getAllMBeansAsMeasurements()
        );
    }

    public static void premain(String args, Instrumentation inst) {
        long interval = CFG.getValue("interval", 10);

        LOG.info("mbean-reporter attached to JVM={} with send interval={} seconds",
                ManagementFactory.getRuntimeMXBean().getName(),
                interval);

        METRICS_DB.createDatabase();
        Executors.newSingleThreadScheduledExecutor()
                .scheduleAtFixedRate(ReportingAgent::send, 0, interval, TimeUnit.SECONDS);
    }
}
