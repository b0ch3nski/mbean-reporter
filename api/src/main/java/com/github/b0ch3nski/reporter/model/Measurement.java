package com.github.b0ch3nski.reporter.model;

import javax.management.ObjectName;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Simplified metric representation that consists of {@code name}, {@code value} and its {@code type}.
 *
 * @author Piotr Bochenski
 */
public final class Measurement {
    private final String name;
    private final Class<?> type;
    private final String value;

    private Measurement(MeasurementBuilder builder) {
        name = builder.name;
        type = builder.type;
        value = builder.value;
    }

    /**
     * Instantiates {@code Measurement} class using fluent builder.
     *
     * @return new {@code MeasurementBuilder} instance
     */
    public static MeasurementBuilder builder() {
        return new MeasurementBuilder();
    }

    public Stream<Measurement> asStream() {
        return Stream.of(this);
    }

    public String getName() {
        return name;
    }

    public Class<?> getType() {
        return type;
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if ((o == null) || (getClass() != o.getClass())) return false;
        Measurement that = (Measurement) o;
        return Objects.equals(name, that.name) &&
                Objects.equals(type, that.type) &&
                Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, type, value);
    }

    @Override
    public String toString() {
        return String.format("%s = [%s] %s", name, type.getSimpleName(), value);
    }

    public static final class MeasurementBuilder {
        private static final Pattern SPACE_PATTERN = Pattern.compile("\\s");
        private static final String NAME_SEPARATOR = ".";

        private String name;
        private Class<?> type;
        private String value;

        private MeasurementBuilder() { }

        private Stream<String> fromObjectName(ObjectName mBeanName) {
            if (mBeanName == null) return Stream.empty();

            Map<String, String> properties = mBeanName.getKeyPropertyList();
            return Stream.of(
                    mBeanName.getDomain(),
                    properties.get("type"),
                    properties.get("name")
            );
        }

        /**
         * Builds {@code Measurement} with name consisting of original MBean object name and optional elements.
         *
         * @param mBeanName         original MBean object name
         * @param additionalElement optional elements appended at the end
         */
        public MeasurementBuilder withName(ObjectName mBeanName, String... additionalElement) {
            name = SPACE_PATTERN.matcher(
                    Stream.concat(
                            fromObjectName(mBeanName),
                            Arrays.stream(additionalElement)
                    )
                            .filter(Objects::nonNull)
                            .map(String::trim)
                            .filter(element -> !element.isEmpty())
                            .collect(Collectors.joining(NAME_SEPARATOR))
            ).replaceAll("_");
            return this;
        }

        /**
         * Builds {@code Measurement} with specified value (type is discovered automatically).
         *
         * @param attrValue metric value
         */
        public MeasurementBuilder withValue(Object attrValue) {
            type = attrValue.getClass();
            value = String.valueOf(attrValue);
            return this;
        }

        /**
         * Finalizing method that produces new instance of {@code Measurement}.
         *
         * @return built {@code Measurement} object
         */
        public Measurement build() {
            return new Measurement(this);
        }
    }
}
