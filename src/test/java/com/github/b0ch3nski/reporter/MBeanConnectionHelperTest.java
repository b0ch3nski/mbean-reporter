package com.github.b0ch3nski.reporter;

import com.github.b0ch3nski.reporter.utils.MetricsTestRule;
import org.junit.Rule;
import org.junit.Test;

import javax.management.*;

import static com.github.b0ch3nski.reporter.MBeanConnectionHelper.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.ThrowableAssert.catchThrowable;

public class MBeanConnectionHelperTest {
    @Rule
    public final MetricsTestRule testRule = new MetricsTestRule("testMeter");

    @Test
    public void shouldFetchMBeanAttrs() {
        // given
        MBeanAttributeInfo expected = new MBeanAttributeInfo(
                "Count", "long", "Attribute exposed for management", true, false, false);

        // when
        MBeanAttributeInfo[] actual = getMBeanAttributeInfos(testRule.getMeterObjName());

        // then
        assertThat(actual).hasSize(6);
        assertThat(actual[0]).isEqualTo(expected);
    }

    @Test
    public void shouldFetchMBeanValue() {
        // given
        long meterValue = 123L;

        // when
        testRule.getMeter().mark(meterValue);
        Object actual = getMBeanAttributeValue(testRule.getMeterObjName(), "Count");

        // then
        assertThat(actual)
                .isInstanceOf(Long.class)
                .isEqualTo(meterValue);
    }

    @Test
    public void shouldHandleBadInput() throws MalformedObjectNameException {
        // given
        ObjectName badObjName = new ObjectName("i.cause:type=problems,name=and-troubles");

        // expect
        Throwable thrownForAttr = catchThrowable(() -> getMBeanAttributeInfos(badObjName));
        Throwable thrownForValue = catchThrowable(() -> getMBeanAttributeValue(badObjName, "pancake"));

        // when
        assertThat(thrownForAttr).isInstanceOf(MBeanConnectionException.class);
        assertThat(thrownForValue).isInstanceOf(MBeanConnectionException.class);
    }
}
