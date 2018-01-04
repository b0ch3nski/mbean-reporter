package com.github.b0ch3nski.reporter.model;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.Test;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import static org.assertj.core.api.Assertions.assertThat;

public class MeasurementTest {

    @Test
    public void shouldBuildSimpleMeasurement() {
        // given
        String name = "testName";
        Integer value = 123;

        // when
        Measurement measurement = Measurement.builder()
                .withName(null, name)
                .withValue(value)
                .build();

        // then
        assertThat(measurement.getName()).isEqualTo(name);
        assertThat(measurement.getType()).isEqualTo(value.getClass());
        assertThat(measurement.getValue()).isEqualTo(String.valueOf(value));
        assertThat(measurement.asStream().count()).isEqualTo(1);
    }

    @Test
    public void shouldCreateCorrectName() throws MalformedObjectNameException {
        // given
        ObjectName objName = new ObjectName("com.domain:name=test-Name,type=test Type");
        String[] elements = { " ", "elem1", null, "", "elem2" };

        // when
        Measurement measurement = Measurement.builder()
                .withName(objName, elements)
                .build();

        // then
        assertThat(measurement.getName()).doesNotContain(" ");
        assertThat(measurement.getName()).isEqualTo("com.domain.test_Type.test-Name.elem1.elem2");
        assertThat(measurement.getType()).isNull();
        assertThat(measurement.getValue()).isNull();
    }

    @Test
    public void shouldCompareClassInstances() {
        EqualsVerifier.forClass(Measurement.class).verify();
    }
}
