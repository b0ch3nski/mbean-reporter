package com.github.b0ch3nski.reporter.mbean;

import com.github.b0ch3nski.reporter.utils.MetricsTestRule;
import org.junit.Rule;
import org.junit.Test;

import javax.management.*;
import java.util.Optional;
import java.util.stream.Stream;

import static com.github.b0ch3nski.reporter.mbean.MBeanConnectionHelper.*;
import static org.assertj.core.api.Assertions.assertThat;

public class MBeanConnectionHelperTest {
    @Rule
    public final MetricsTestRule testRule = new MetricsTestRule("testMeter");

    @Test
    public void shouldFetchMBeanAttrs() {
        // given
        MBeanAttributeInfo expected = new MBeanAttributeInfo(
                "Count", "long", "Attribute exposed for management", true, false, false);

        // when
        Stream<MBeanAttributeInfo> actual = getMBeanAttributeInfos(testRule.getMeterObjName());

        // then
        assertThat(actual)
                .hasSize(6)
                .first().isEqualTo(expected);
    }

    @Test
    public void shouldFetchMBeanValue() {
        // given
        long meterValue = 123L;

        // when
        testRule.getMeter().mark(meterValue);
        Optional<Object> actual = getMBeanAttributeValue(testRule.getMeterObjName(), "Count");

        // then
        assertThat(actual)
                .isPresent()
                .hasValue(meterValue);
    }

    @Test
    public void shouldHandleBadInput() throws MalformedObjectNameException {
        // given
        ObjectName badObjName = new ObjectName("i.cause:type=problems,name=and-troubles");

        // when
        Stream<MBeanAttributeInfo> actualAttrInfo = getMBeanAttributeInfos(badObjName);
        Optional<Object> actualValue = getMBeanAttributeValue(badObjName, "nope");

        // then
        assertThat(actualAttrInfo).isEmpty();
        assertThat(actualValue).isNotPresent();
    }
}
