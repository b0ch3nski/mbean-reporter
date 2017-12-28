package com.github.b0ch3nski.reporter;

import com.github.b0ch3nski.reporter.model.Measurement;

import javax.management.MBeanAttributeInfo;
import javax.management.ObjectName;
import javax.management.openmbean.*;
import java.util.Arrays;
import java.util.List;
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
                            && INVALID_ATTR_NAME_PARTS.stream().noneMatch(name -> attribute.getName().contains(name))
            );

    private MBeanProcessingHelper() { }

    private static String getCompDataAttrType(String attrType) {
        return Object.class.getName().equals(attrType)
                ? "long"
                : attrType;
    }

    private static Stream<Measurement> buildMeasuresFromCompData(ObjectName mBeanName, String attrName, CompositeData data) {
        CompositeType compositeType = data.getCompositeType();
        return compositeType.keySet().parallelStream()
                .map(key ->
                        Measurement.builder()
                                .withName(mBeanName, attrName, key)
                                .withType(getCompDataAttrType(compositeType.getTypeName()))
                                .withValue(data.get(key))
                                .build()
                );
    }

    private static Stream<Measurement> buildMeasurements(ObjectName mBeanName, MBeanAttributeInfo attribute) {
        String attrName = attribute.getName();
        Object attrValue = MBeanConnectionHelper.getMBeanAttributeValue(mBeanName, attrName);

        if (attrValue instanceof CompositeDataSupport)
            return buildMeasuresFromCompData(mBeanName, attrName, (CompositeDataSupport) attrValue);
        else if (attrValue != null)
            return Measurement.builder()
                    .withName(mBeanName, attrName)
                    .withType(attribute.getType())
                    .withValue(attrValue)
                    .build().asStream();
        else return Stream.empty();
    }

    static Stream<Measurement> getMBeanAsMeasurements(ObjectName mBeanName) {
        return Arrays.stream(MBeanConnectionHelper.getMBeanAttributeInfos(mBeanName))
                .filter(MBEAN_ATTR_PREDICATE)
                .flatMap(attribute -> buildMeasurements(mBeanName, attribute));
    }

    static Stream<Measurement> getAllMBeansAsMeasurements() {
        return MBeanConnectionHelper.getAllMBeans()
                .parallelStream()
                .flatMap(mBean -> getMBeanAsMeasurements(mBean.getObjectName()));
    }
}
