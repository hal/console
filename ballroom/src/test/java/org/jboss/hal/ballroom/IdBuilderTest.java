package org.jboss.hal.ballroom;

import org.jboss.hal.resources.IdBuilder;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Harald Pehl
 */
public class IdBuilderTest {

    @Test
    public void simple() {
        assertEquals("foo", IdBuilder.build("foo"));
    }

    @Test
    public void complex() {
        assertEquals("foo-bar-1-2-3", IdBuilder.build("foo", "bar", "1-2", "3"));
    }

    @Test
    public void emptyNull() {
        assertEquals("foo-bar", IdBuilder.build("foo", "", null, "", "", "bar", ""));
    }

    @Test(expected = IllegalArgumentException.class)
    public void illegal() {
        IdBuilder.build(null);
    }

    @Test
    @SuppressWarnings("DuplicateStringLiteralInspection")
    public void asId() {
        assertEquals("lorem-ipsum", IdBuilder.asId("lorem-ipsum"));
        assertEquals("lorem-ipsum", IdBuilder.asId("Lorem Ipsum"));
        assertEquals("l0rem-ip5um", IdBuilder.asId("l0rem-ip5um"));
        assertEquals("lorem--ipsum", IdBuilder.asId("lorem §±!@#$%^&*()=_+[]{};'\\:\"|,./<>?`~ ipsum"));
    }
}