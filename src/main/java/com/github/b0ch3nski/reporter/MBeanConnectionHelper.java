package com.github.b0ch3nski.reporter;

import javax.management.*;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;

final class MBeanConnectionHelper {
    private static final MBeanServerConnection CONNECTION = ManagementFactory.getPlatformMBeanServer();

    private MBeanConnectionHelper() { }

    static Stream<MBeanAttributeInfo> getMBeanAttributeInfos(ObjectName mBeanName) {
        try {
            return Arrays.stream(
                    CONNECTION.getMBeanInfo(mBeanName).getAttributes()
            );
        } catch (JMException | IOException ignored) {
            return Stream.empty();
        }
    }

    static Optional<Object> getMBeanAttributeValue(ObjectName mBeanName, String attrName) {
        try {
            return Optional.ofNullable(
                    CONNECTION.getAttribute(mBeanName, attrName)
            );
        } catch (JMException | IOException ignored) {
            return Optional.empty();
        }
    }

    static Stream<ObjectInstance> getAllMBeans() {
        try {
            return CONNECTION.queryMBeans(null, null)
                    .parallelStream();
        } catch (IOException ignored) {
            return Stream.empty();
        }
    }
}
