package com.github.b0ch3nski.reporter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.instrument.Instrumentation;
import java.lang.management.ManagementFactory;

public final class Agent {
    private static final Logger log = LoggerFactory.getLogger(Agent.class);

    private Agent() { }

    public static void premain(String args, Instrumentation inst) {
        log.info("mbean-reporter attached to JVM={}", ManagementFactory.getRuntimeMXBean().getName());
    }
}
