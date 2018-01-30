package com.github.b0ch3nski.reporter.mbean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.*;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;

final class MBeanConnectionHelper {
    private static final Logger LOG = LoggerFactory.getLogger(MBeanConnectionHelper.class);
    private static final MBeanServerConnection CONNECTION = ManagementFactory.getPlatformMBeanServer();

    private MBeanConnectionHelper() { }

    private static void logDetails(Throwable thr) {
        LOG.debug("MBean connection failed", thr);
    }

    static Stream<MBeanAttributeInfo> getMBeanAttributeInfos(ObjectName mBeanName) {
        try {
            return Arrays.stream(
                    CONNECTION.getMBeanInfo(mBeanName).getAttributes()
            );
        } catch (JMException | IOException e) {
            LOG.warn("Failed to get attribute info for MBean={}, cause={}", mBeanName, e.getMessage());
            logDetails(e);
            return Stream.empty();
        }
    }

    static Optional<Object> getMBeanAttributeValue(ObjectName mBeanName, String attrName) {
        try {
            return Optional.ofNullable(
                    CONNECTION.getAttribute(mBeanName, attrName)
            );
        } catch (JMException | IOException e) {
            LOG.warn("Failed to get value of attribute={} for MBean={}, cause={}", attrName, mBeanName, e.getMessage());
            logDetails(e);
            return Optional.empty();
        }
    }

    static Stream<ObjectInstance> getAllMBeans() {
        try {
            return CONNECTION.queryMBeans(null, null)
                    .parallelStream();
        } catch (IOException e) {
            LOG.warn("Failed to query MBeans, cause={}", e.getMessage());
            logDetails(e);
            return Stream.empty();
        }
    }
}
