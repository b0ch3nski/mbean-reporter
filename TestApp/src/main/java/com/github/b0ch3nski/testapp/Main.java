package com.github.b0ch3nski.testapp;

import com.codahale.metrics.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public final class Main {
    private static final Logger log = LoggerFactory.getLogger(Main.class);

    private Main() { }

    private static void randomSleep(int min, int max, TimeUnit unit) {
        int duration = ThreadLocalRandom.current().nextInt(min, max + 1);
        log.trace("sleeping for {} {}", duration, unit);

        try {
            unit.sleep(duration);
        } catch (InterruptedException ignored) { }
    }

    private static void shutdown() {
        log.debug("shutting down");
    }

    public static void main(String[] args) {
        log.debug("starting");

        Runtime.getRuntime().addShutdownHook(
                new Thread(Main::shutdown, "shutdownHook")
        );

        MetricRegistry registry = new MetricRegistry();
        JmxReporter.forRegistry(registry)
                .build()
                .start();

        Meter testMeter = registry.meter(
                MetricRegistry.name(Main.class, "test")
        );

        while(true) {
            testMeter.mark();
            randomSleep(500, 1500, TimeUnit.MILLISECONDS);
        }
    }
}
