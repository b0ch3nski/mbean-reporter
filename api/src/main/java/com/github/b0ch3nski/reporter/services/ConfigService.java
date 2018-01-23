package com.github.b0ch3nski.reporter.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.Map.Entry;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    private ConfigService() {
        config = fromEnvAndProps(System::getenv, System::getProperties);
        LOG.debug("Loaded configuration={}", config);
    }

    public static synchronized ConfigService getInstance() {
        if (service == null)
            service = new ConfigService();
        return service;
    }

    public String getValue(String key, String defVal) {
        return config.getOrDefault(key.toLowerCase(), defVal);
    }

    public long getValue(String key, long defVal) {
        try {
            return Long.valueOf(config.get(key.toLowerCase()));
        } catch (NumberFormatException ignored) {
            return defVal;
        }
    }
}
