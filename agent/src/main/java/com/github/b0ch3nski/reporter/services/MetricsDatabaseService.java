package com.github.b0ch3nski.reporter.services;

import com.github.b0ch3nski.reporter.persistence.MetricsDatabase;
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

    private Optional<MetricsDatabase> getDatabase(String reqClassName) {
        try {
            Class<?> reqClass = Class.forName(reqClassName);
            return implementations.stream()
                    .filter(impl -> Objects.equals(impl.getClass(), reqClass))
                    .findFirst();
        } catch (ClassNotFoundException ignored) {
            LOG.warn("Classpath doesn't contain requested class={}", reqClassName);
            return Optional.empty();
        }
    }

    public MetricsDatabase getDatabase(String reqClassName, String defClassName) {
        Optional<MetricsDatabase> reqImpl = getDatabase(reqClassName);

        if (reqImpl.isPresent()) {
            LOG.info("Using 'MetricsDatabase' implementation={}", reqClassName);
            return reqImpl.get();
        } else {
            LOG.warn("Unable to find 'MetricsDatabase' implementation={} falling back to default={}",
                    reqClassName, defClassName);
            return getDatabase(defClassName).get();
        }
    }
}
