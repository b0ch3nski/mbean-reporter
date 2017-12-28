package com.github.b0ch3nski.reporter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.*;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.Set;

final class MBeanConnectionHelper {
    private static final MBeanServerConnection CONNECTION = ManagementFactory.getPlatformMBeanServer();

    private MBeanConnectionHelper() { }

    static MBeanAttributeInfo[] getMBeanAttributeInfos(ObjectName mBeanName) {
        try {
            return CONNECTION.getMBeanInfo(mBeanName).getAttributes();
        } catch (JMException | IOException e) {
            throw new MBeanConnectionException(e);
        }
    }

    static Object getMBeanAttributeValue(ObjectName mBeanName, String attrName) {
        try {
            return CONNECTION.getAttribute(mBeanName, attrName);
        } catch (JMException | IOException e) {
            throw new MBeanConnectionException(e);
        }
    }

    static Set<ObjectInstance> getAllMBeans() {
        try {
            return CONNECTION.queryMBeans(null, null);
        } catch (IOException e) {
            throw new MBeanConnectionException(e);
        }
    }

    static class MBeanConnectionException extends RuntimeException {
        private static final Logger LOG = LoggerFactory.getLogger(MBeanConnectionException.class);

        MBeanConnectionException(Throwable throwable) {
            LOG.info("Failed to process MBeans, cause={}", throwable.getMessage());
        }
    }
}
