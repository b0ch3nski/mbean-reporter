package com.github.b0ch3nski.reporter.services;

import org.junit.Test;

import java.util.*;
import java.util.function.Supplier;

import static com.github.b0ch3nski.reporter.services.ConfigService.fromEnvAndProps;
import static org.assertj.core.api.Assertions.assertThat;

public class ConfigServiceTest {
    private static final Supplier<Map<String, String>> ENV;
    private static final Supplier<Properties> PROPS;
    static {
        ENV = () -> {
            Map<String, String> toReturn = new HashMap<>();
            toReturn.put("REPORTER_TEST", "env-test");
            toReturn.put("SOME_ENV", "ignored");
            return toReturn;
        };
        PROPS = () -> {
            Properties toReturn = new Properties();
            toReturn.setProperty("reporter.test", "props-test");
            toReturn.setProperty("other.prop", "ignored");
            return toReturn;
        };
    }

    @Test
    public void shouldCreateConfigFromEnv() {
        // when
        Map<String, String> actual = fromEnvAndProps(ENV, () -> null);

        // then
        assertThat(actual)
                .hasSize(1)
                .containsKey("test")
                .containsValue("env-test");
    }

    @Test
    public void shouldCreateConfigFromProps() {
        // when
        Map<String, String> actual = fromEnvAndProps(() -> null, PROPS);

        // then
        assertThat(actual)
                .hasSize(1)
                .containsKey("test")
                .containsValue("props-test");
    }

    @Test
    public void shouldOverrideEnvWithProp() {
        // when
        Map<String, String> actual = fromEnvAndProps(ENV, PROPS);

        // then
        assertThat(actual)
                .hasSize(1)
                .containsKey("test")
                .doesNotContainValue("env-test")
                .containsValue("props-test");
    }
}
