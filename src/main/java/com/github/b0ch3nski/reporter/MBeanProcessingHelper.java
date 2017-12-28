package com.github.b0ch3nski.reporter;

import com.github.b0ch3nski.reporter.model.Measurement;

import javax.management.MBeanAttributeInfo;
import javax.management.ObjectName;
import javax.management.openmbean.*;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

final class MBeanProcessingHelper {

    private static final List<String> VALID_ATTR_TYPES =
            Arrays.asList("double", "long", Object.class.getName(), CompositeData.class.getName());

    private static final List<String> INVALID_ATTR_NAME_PARTS =
            Arrays.asList("UsageThreshold", "LastGcInfo");

    private static final Predicate<MBeanAttributeInfo> MBEAN_ATTR_PREDICATE =
            attribute -> (
                    attribute.isReadable()
                            && VALID_ATTR_TYPES.contains(attribute.getType())
                            && INVALID_ATTR_NAME_PARTS.stream()
                            .noneMatch(name -> attribute.getName().contains(name))
            );

    private MBeanProcessingHelper() { }

    private static String getCompDataAttrType(String attrType) {
        return Object.class.getName().equals(attrType)
                ? "long"
                : attrType;
    }

    private static Stream<Measurement> buildMeasuresFromCompData(ObjectName mBeanName, String attrName, CompositeData compData) {
        CompositeType compType = compData.getCompositeType();

        return compType.keySet().stream()
                .map(key ->
                        Measurement.builder()
                                .withName(mBeanName, attrName, key)
                                .withType(getCompDataAttrType(compType.getTypeName()))
                                .withValue(compData.get(key))
                                .build()
                );
    }

    private static Stream<Measurement> buildMeasurements(ObjectName mBeanName, MBeanAttributeInfo attribute) {
        String attrName = attribute.getName();
        Optional<Object> optionalAttrValue = MBeanConnectionHelper.getMBeanAttributeValue(mBeanName, attrName);

        if (!optionalAttrValue.isPresent()) return Stream.empty();
        Object attrValue = optionalAttrValue.get();

        if (attrValue instanceof CompositeDataSupport)
            return buildMeasuresFromCompData(mBeanName, attrName, (CompositeDataSupport) attrValue);
        else
            return Measurement.builder()
                    .withName(mBeanName, attrName)
                    .withType(attribute.getType())
                    .withValue(attrValue)
                    .build().asStream();
    }

    static Stream<Measurement> getMBeanAsMeasurements(ObjectName mBeanName) {
        return MBeanConnectionHelper.getMBeanAttributeInfos(mBeanName)
                .filter(MBEAN_ATTR_PREDICATE)
                .flatMap(attribute -> buildMeasurements(mBeanName, attribute));
    }

    static Stream<Measurement> getAllMBeansAsMeasurements() {
        return MBeanConnectionHelper.getAllMBeans()
                .flatMap(mBean -> getMBeanAsMeasurements(mBean.getObjectName()));
    }
}
