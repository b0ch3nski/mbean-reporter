package com.github.b0ch3nski.reporter.services;

import com.github.b0ch3nski.reporter.persistence.MetricsDatabase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Provides access to every loaded and accessible implementation of {@code MetricsDatabase} through Java Service
 * Provider Interface.
 *
 * @author Piotr Bochenski
 */
public final class MetricsDatabaseService {
    private static final Logger LOG = LoggerFactory.getLogger(MetricsDatabaseService.class);
    private final Set<MetricsDatabase> implementations = new HashSet<>();
    private static MetricsDatabaseService service;

    private MetricsDatabaseService() {
        ServiceLoader.load(MetricsDatabase.class).iterator()
                .forEachRemaining(implementations::add);

        LOG.debug("Found interface={} implementations={}", MetricsDatabase.class.getSimpleName(), implementations);
    }

    /**
     * Provides access to {@code MetricsDatabaseService} class.
     *
     * @return new class instance if it was not initialized before, otherwise initializes global singleton
     */
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

    /**
     * Returns {@code MetricsDatabase} instance for requested implementation {@code reqClassName} if it's available,
     * otherwise tries to return provided {@code defClassName}.
     *
     * @param reqClassName fully-qualified class name of requested implementation
     * @param defClassName fully-qualified class name of default fallback implementation
     * @return instance of {@code reqClassName}, or {@code defClassName} when not available
     */
    public MetricsDatabase getDatabase(String reqClassName, String defClassName) {
        Optional<MetricsDatabase> reqImpl = getDatabase(reqClassName);

        if (reqImpl.isPresent()) {
            LOG.info("Using interface={} implementation={}", MetricsDatabase.class.getSimpleName(), reqClassName);
            return reqImpl.get();
        } else {
            LOG.warn("Unable to find interface={} implementation={}, falling back to default={}",
                    MetricsDatabase.class.getSimpleName(), reqClassName, defClassName);
            return getDatabase(defClassName).get();
        }
    }
}
