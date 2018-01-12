package com.github.b0ch3nski.reporter.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class ConfigService {
    private static final Logger LOG = LoggerFactory.getLogger(ConfigService.class);
    private static final Pattern CFG_PATTERN = Pattern.compile("^reporter[_.]+");
    private static ConfigService service;
    private final Map<String, String> config;

    private ConfigService() {
        config = Stream.of(
                System.getenv(),
                System.getProperties()
        ).flatMap(map ->
                map.entrySet().stream()
        ).filter(entry ->
                CFG_PATTERN.matcher(String.valueOf(entry.getKey())).lookingAt()
        ).collect(
                Collectors.toMap(
                        entry -> CFG_PATTERN.matcher(String.valueOf(entry.getKey())).replaceFirst(""),
                        entry -> String.valueOf(entry.getValue()),
                        (key1, key2) -> key2)
        );
        LOG.debug("Loaded configuration={}", config);
    }

    public static synchronized ConfigService getInstance() {
        if (service == null)
            service = new ConfigService();
        return service;
    }

    public String getValue(String key, String defVal) {
        return config.getOrDefault(key, defVal);
    }
}
