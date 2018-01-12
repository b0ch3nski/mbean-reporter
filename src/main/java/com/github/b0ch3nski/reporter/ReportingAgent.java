package com.github.b0ch3nski.reporter;

import com.github.b0ch3nski.reporter.mbean.MBeanProcessingHelper;
import com.github.b0ch3nski.reporter.persistence.MetricsDatabase;
import com.github.b0ch3nski.reporter.services.ConfigService;
import com.github.b0ch3nski.reporter.services.MetricsDatabaseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.instrument.Instrumentation;
import java.lang.management.ManagementFactory;
import java.util.concurrent.*;

public final class ReportingAgent {
    private static final Logger LOG = LoggerFactory.getLogger(ReportingAgent.class);
    private static final ScheduledExecutorService EXECUTOR = Executors.newSingleThreadScheduledExecutor();
    private static final MetricsDatabase METRICS_DB;

    static {
        String defDbImpl = "com.github.b0ch3nski.reporter.persistence.InfluxDB";
        METRICS_DB =
                MetricsDatabaseService.getInstance().getDatabase(
                        ConfigService.getInstance().getValue("dbImpl", defDbImpl),
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
        LOG.info("mbean-reporter attached to JVM={}", ManagementFactory.getRuntimeMXBean().getName());

        METRICS_DB.createDatabase();
        EXECUTOR.scheduleAtFixedRate(ReportingAgent::send, 0, 10, TimeUnit.SECONDS);
    }
}
