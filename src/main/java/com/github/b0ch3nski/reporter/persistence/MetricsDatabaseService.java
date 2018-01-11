package com.github.b0ch3nski.reporter.persistence;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public final class MetricsDatabaseService {
    private static final Logger LOG = LoggerFactory.getLogger(MetricsDatabaseService.class);
    private final Set<MetricsDatabase> implementations = new HashSet<>();
    private static MetricsDatabaseService service;

    private MetricsDatabaseService() {
        ServiceLoader.load(MetricsDatabase.class).iterator()
                .forEachRemaining(implementations::add);

        LOG.debug("Found 'MetricsDatabase' implementations={}", implementations);
    }

    public static synchronized MetricsDatabaseService getInstance() {
        if (service == null)
            service = new MetricsDatabaseService();
        return service;
    }

    public Optional<MetricsDatabase> getDatabase(String className) {
        try {
            Class<?> cls = Class.forName(className);
            return implementations.stream()
                    .filter(impl -> Objects.equals(impl.getClass(), cls))
                    .findAny();
        } catch (ClassNotFoundException ignored) {
            LOG.warn("Classpath doesn't contain requested class={}", className);
            return Optional.empty();
        }
    }
}
