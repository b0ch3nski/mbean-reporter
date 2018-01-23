package com.github.b0ch3nski.reporter.utils;

import com.codahale.metrics.*;
import org.junit.rules.ExternalResource;

import javax.management.ObjectName;

public final class MetricsTestRule extends ExternalResource {
    private final String meterName;
    private MetricRegistry registry;
    private JmxReporter reporter;
    private Meter testMeter;
    private ObjectName meterObjName;

    public MetricsTestRule(String meterName) {
        this.meterName = meterName;
    }

    @Override
    protected void before() throws Throwable {
        registry = new MetricRegistry();
        reporter = JmxReporter.forRegistry(registry).build();
        reporter.start();

        testMeter = registry.meter(meterName);
        meterObjName = new ObjectName("metrics:name=" + meterName);
    }

    @Override
    protected void after() {
        registry.remove(meterName);
        reporter.stop();
    }

    public Meter getMeter() {
        return testMeter;
    }

    public ObjectName getMeterObjName() {
        return meterObjName;
    }
}
