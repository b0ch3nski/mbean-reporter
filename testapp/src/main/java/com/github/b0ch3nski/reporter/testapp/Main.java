package com.github.b0ch3nski.reporter.testapp;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.jmx.JmxReporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

/**
 * @author Piotr Bochenski
 */
public final class Main {
    private static final Logger LOG = LoggerFactory.getLogger(Main.class);

    private Main() { }

    private static void randomSleep(int min, int max, TimeUnit unit) {
        int duration = ThreadLocalRandom.current().nextInt(min, max + 1);
        LOG.trace("sleeping for {} {}", duration, unit);

        try {
            unit.sleep(duration);
        } catch (InterruptedException ignored) { }
    }

    private static void shutdown() {
        LOG.debug("shutting down");
    }

    public static void main(String[] args) {
        LOG.debug("starting");

        Runtime.getRuntime().addShutdownHook(
                new Thread(Main::shutdown, "shutdownHook")
        );

        MetricRegistry registry = new MetricRegistry();
        JmxReporter.forRegistry(registry).build().start();

        Meter testMeter = registry.meter(
                MetricRegistry.name(Main.class, "test test", "more tests")
        );

        while (true) {
            testMeter.mark();
            randomSleep(500, 1500, TimeUnit.MILLISECONDS);
        }
    }
}
