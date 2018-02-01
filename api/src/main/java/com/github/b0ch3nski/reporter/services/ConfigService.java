package com.github.b0ch3nski.reporter.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Provides easy access to runtime configuration which is created from environment variables and Java execution
 * properties (override supported). Variable name must start with {@code reporter} prefix followed by dot/underscore and
 * configuration key name (all case insensitive), for example:
 * <ul>
 * <li>environment variable {@code REPORTER_NAME=test} will result in {@code name=test} configuration</li>
 * <li>execution property {@code -Dreporter.dataBase=test} will result in {@code database=test} configuration</li>
 * </ul>
 *
 * @author Piotr Bochenski
 */
public final class ConfigService {
    private static final Logger LOG = LoggerFactory.getLogger(ConfigService.class);
    private static final Pattern KEY_PATTERN = Pattern.compile("^reporter[_.]+", Pattern.CASE_INSENSITIVE);
    private static ConfigService service;
    private final Map<String, String> config;

    private static Matcher getMatcherForKey(Entry<?, ?> entry) {
        return KEY_PATTERN.matcher(
                String.valueOf(entry.getKey())
        );
    }

    static Map<String, String> fromEnvAndProps(Supplier<Map<String, String>> env, Supplier<Properties> props) {
        return Stream.of(env.get(), props.get())
                .filter(Objects::nonNull)
                .flatMap(map -> map.entrySet().stream())
                .filter(entry -> getMatcherForKey(entry).lookingAt())
                .collect(Collectors.toMap(
                        entry -> getMatcherForKey(entry).replaceFirst("").toLowerCase(),
                        entry -> String.valueOf(entry.getValue()),
                        (key1, key2) -> key2)
                );
    }

    private static String getHostName() {
        try {
            return Optional
                    .ofNullable(System.getenv("HOSTNAME"))
                    .orElse(InetAddress.getLocalHost().getCanonicalHostName());
        } catch (UnknownHostException e) {
            LOG.warn("Failed to resolve hostname - returning 'unknown', cause={}", e.getMessage());
            return "unknown";
        }
    }

    private ConfigService() {
        config = fromEnvAndProps(System::getenv, System::getProperties);
        config.putIfAbsent("hostname", getHostName());

        LOG.debug("Loaded configuration={}", config);
    }

    /**
     * Provides access to {@code ConfigService} class.
     *
     * @return new class instance if it was not initialized before, otherwise initializes global singleton
     */
    public static synchronized ConfigService getInstance() {
        if (service == null)
            service = new ConfigService();
        return service;
    }

    /**
     * Returns value for specified config {@code key} if it exists, otherwise returns provided {@code defVal}.
     *
     * @param key    config key to look for
     * @param defVal default value that is returned when {@code key} doesn't exist
     * @return value of specified key, or {@code defVal} when not found
     */
    public String getValue(String key, String defVal) {
        return config.getOrDefault(key.toLowerCase(), defVal);
    }

    /**
     * Returns value for specified config {@code key} if it exists, otherwise returns provided {@code defVal}.
     *
     * @param key    config key to look for
     * @param defVal default value that is returned when {@code key} doesn't exist
     * @return value of specified key, or {@code defVal} when not found
     */
    public long getValue(String key, long defVal) {
        try {
            return Long.valueOf(config.get(key.toLowerCase()));
        } catch (NumberFormatException ignored) {
            return defVal;
        }
    }
}
