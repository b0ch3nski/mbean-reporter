package com.github.b0ch3nski.reporter;

import com.github.b0ch3nski.reporter.http.HttpRequestException;
import com.github.b0ch3nski.reporter.mbean.MBeanProcessingHelper;
import com.github.b0ch3nski.reporter.persistence.MetricsDatabase;
import com.github.b0ch3nski.reporter.persistence.MetricsDatabaseException;
import com.github.b0ch3nski.reporter.services.ConfigService;
import com.github.b0ch3nski.reporter.services.MetricsDatabaseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.instrument.Instrumentation;
import java.lang.management.ManagementFactory;
import java.util.concurrent.*;

/**
 * Lightweight JVM MBean metrics reporter that can be easily extended to support any database.
 * <p>Following configuration is supported:
 * <ul>
 * <li>{@code interval} - reporting period in seconds, default: {@code 10}</li>
 * <li>{@code dbImpl} - fully qualified class name of {@code MetricsDatabase} implementation, default:
 * {@code com.github.b0ch3nski.reporter.persistence.InfluxDB}</li>
 * </ul>
 *
 * @author Piotr Bochenski
 */
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

    private static void logThrowableDetails(Throwable thr) {
        LOG.warn(thr.getMessage());

        if (LOG.isDebugEnabled() && (thr.getCause() instanceof HttpRequestException)) {
            LOG.debug("Got HTTP response={}", ((HttpRequestException) thr.getCause()).getResponse());
        }
    }

    private static CompletionStage<?> createDatabase(ScheduledExecutorService executor, long interval) {
        CompletableFuture<?> future = new CompletableFuture<>();

        ScheduledFuture<?> scheduledFuture = executor.scheduleWithFixedDelay(() -> {
            try {
                METRICS_DB.createDatabase();
                future.complete(null);
            } catch (MetricsDatabaseException e) {
                logThrowableDetails(e);
            }
        }, 0, interval, TimeUnit.SECONDS);

        future.thenRunAsync(() -> scheduledFuture.cancel(false), executor);
        return future;
    }

    private static ScheduledFuture<?> sendData(ScheduledExecutorService executor, long interval) {
        return executor.scheduleWithFixedDelay(() -> {
            try {
                METRICS_DB.sendMeasurements(
                        MBeanProcessingHelper.getAllMBeansAsMeasurements()
                );
            } catch (MetricsDatabaseException e) {
                logThrowableDetails(e);
            }
        }, 0, interval, TimeUnit.SECONDS);
    }

    public static void premain(String args, Instrumentation inst) {
        long interval = CFG.getValue("interval", 10);

        LOG.info("mbean-reporter attached to JVM={} with send interval={} seconds",
                ManagementFactory.getRuntimeMXBean().getName(),
                interval);

        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        createDatabase(executor, interval)
                .thenRunAsync(() -> sendData(executor, interval), executor);
    }
}
