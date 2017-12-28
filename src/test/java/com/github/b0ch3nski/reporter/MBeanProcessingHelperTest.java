package com.github.b0ch3nski.reporter;

import com.github.b0ch3nski.reporter.MBeanConnectionHelper.MBeanConnectionException;
import com.github.b0ch3nski.reporter.model.Measurement;
import com.github.b0ch3nski.reporter.utils.MetricsTestRule;
import org.junit.Rule;
import org.junit.Test;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import java.util.List;
import java.util.stream.Collectors;

import static com.github.b0ch3nski.reporter.MBeanProcessingHelper.getMBeanAsMeasurements;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.ThrowableAssert.catchThrowable;

public class MBeanProcessingHelperTest {
    @Rule
    public final MetricsTestRule testRule = new MetricsTestRule("testMeter");

    @Test
    public void shouldProcessMBeanToMeasurement() {
        // given
        long meterValue = 321L;

        Measurement expected = Measurement.builder()
                .withName(testRule.getMeterObjName(), "Count")
                .withType("long")
                .withValue(meterValue)
                .build();

        // when
        testRule.getMeter().mark(meterValue);

        List<Measurement> actual = getMBeanAsMeasurements(testRule.getMeterObjName())
                .collect(Collectors.toList());

        // then
        assertThat(actual)
                .doesNotContainNull()
                .hasOnlyElementsOfType(Measurement.class)
                .hasSize(5)
                .first().isEqualTo(expected);
    }

    @Test
    public void shouldHandleBadInput() throws MalformedObjectNameException {
        // given
        ObjectName badObjName = new ObjectName("i.cause:type=problems,name=and-troubles");

        // expect
        Throwable thrown = catchThrowable(() -> getMBeanAsMeasurements(badObjName));

        // when
        assertThat(thrown).isInstanceOf(MBeanConnectionException.class);
    }
}
