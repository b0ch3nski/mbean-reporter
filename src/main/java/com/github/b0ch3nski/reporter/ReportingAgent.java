package com.github.b0ch3nski.reporter;

import com.github.b0ch3nski.reporter.model.Measurement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.instrument.Instrumentation;
import java.lang.management.ManagementFactory;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public final class ReportingAgent {
    private static final Logger LOG = LoggerFactory.getLogger(ReportingAgent.class);
    private static final ScheduledExecutorService EXECUTOR = Executors.newSingleThreadScheduledExecutor();

    private ReportingAgent() { }

    private static void print() {
        String measurements = MBeanProcessingHelper.getAllMBeansAsMeasurements()
                .map(Measurement::toString)
                .collect(Collectors.joining("\n"));

        LOG.info("\n{}\n", measurements);
    }

    public static void premain(String args, Instrumentation inst) {
        LOG.info("mbean-reporter attached to JVM={}", ManagementFactory.getRuntimeMXBean().getName());

        EXECUTOR.scheduleAtFixedRate(ReportingAgent::print, 0, 10, TimeUnit.SECONDS);
    }
}
