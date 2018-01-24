package com.github.b0ch3nski.reporter.mbean;

import com.github.b0ch3nski.reporter.model.Measurement;

import javax.management.MBeanAttributeInfo;
import javax.management.ObjectName;
import javax.management.openmbean.CompositeData;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

public final class MBeanProcessingHelper {

    private static final List<String> VALID_ATTR_TYPES =
            Arrays.asList("int", "long", "double", CompositeData.class.getName());

    private static final List<String> INVALID_ATTR_NAME_PARTS =
            Arrays.asList("UsageThreshold", "LastGcInfo");

    private static final Predicate<MBeanAttributeInfo> MBEAN_ATTR_PREDICATE =
            attribute -> (
                    attribute.isReadable()
                            && VALID_ATTR_TYPES.contains(attribute.getType())
                            && INVALID_ATTR_NAME_PARTS.stream()
                            .noneMatch(badPart -> attribute.getName().contains(badPart))
            );

    private MBeanProcessingHelper() { }

    private static Stream<Measurement> buildMeasuresFromCompData(ObjectName mBeanName, String attrName, CompositeData compData) {
        return compData.getCompositeType().keySet().stream()
                .map(key ->
                        Measurement.builder()
                                .withName(mBeanName, attrName, key)
                                .withValue(compData.get(key))
                                .build()
                );
    }

    private static Stream<Measurement> buildMeasurements(ObjectName mBeanName, MBeanAttributeInfo attribute) {
        String attrName = attribute.getName();
        Optional<Object> optionalAttrValue = MBeanConnectionHelper.getMBeanAttributeValue(mBeanName, attrName);

        if (!optionalAttrValue.isPresent()) return Stream.empty();
        Object attrValue = optionalAttrValue.get();

        // values of type TabularData and arrays aren't supported as they don't carry any useful metrics
        if (attrValue instanceof CompositeData)
            return buildMeasuresFromCompData(mBeanName, attrName, (CompositeData) attrValue);
        else
            return Measurement.builder()
                    .withName(mBeanName, attrName)
                    .withValue(attrValue)
                    .build().asStream();
    }

    static Stream<Measurement> getMBeanAsMeasurements(ObjectName mBeanName) {
        return MBeanConnectionHelper.getMBeanAttributeInfos(mBeanName)
                .filter(MBEAN_ATTR_PREDICATE)
                .flatMap(attribute -> buildMeasurements(mBeanName, attribute));
    }

    public static Stream<Measurement> getAllMBeansAsMeasurements() {
        return MBeanConnectionHelper.getAllMBeans()
                .flatMap(mBean -> getMBeanAsMeasurements(mBean.getObjectName()));
    }
}
