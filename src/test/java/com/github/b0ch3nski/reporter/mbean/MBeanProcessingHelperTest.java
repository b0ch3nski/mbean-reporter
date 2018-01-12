package com.github.b0ch3nski.reporter.mbean;

import com.github.b0ch3nski.reporter.model.Measurement;
import com.github.b0ch3nski.reporter.utils.MetricsTestRule;
import org.junit.*;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.github.b0ch3nski.reporter.mbean.MBeanProcessingHelper.getMBeanAsMeasurements;
import static org.assertj.core.api.Assertions.assertThat;

public class MBeanProcessingHelperTest {
    @Rule
    public final MetricsTestRule testRule = new MetricsTestRule("testMeter");

    @Test
    public void shouldProcessMBeanToMeasurements() {
        // given
        long meterValue = 321L;

        Measurement expected = Measurement.builder()
                .withName(testRule.getMeterObjName(), "Count")
                .withValue(meterValue)
                .build();

        // when
        testRule.getMeter().mark(meterValue);
        Stream<Measurement> actual = getMBeanAsMeasurements(testRule.getMeterObjName());

        // then
        assertThat(actual)
                .hasSize(5)
                .first().isEqualTo(expected);
    }

    @Test
    public void shouldHandleBadInput() throws MalformedObjectNameException {
        // given
        ObjectName badObjName = new ObjectName("i.cause:type=problems,name=and-troubles");

        // when
        Stream<Measurement> actual = getMBeanAsMeasurements(badObjName);

        // then
        assertThat(actual).isEmpty();
    }

    @Test
    @Ignore
    public void printAllMBeansAsMeasurements() {
        List<Measurement> measurementList = MBeanProcessingHelper.getAllMBeansAsMeasurements()
                .collect(Collectors.toList());

        String measurements = measurementList.stream()
                .map(Measurement::toString)
                .collect(Collectors.joining("\n"));

        System.out.println(String.format(
                "Got %s measurements:\n%s", measurementList.size(), measurements));
    }
}
