package org.jboss.hal.ballroom;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Harald Pehl
 */
public class IdTest {

    @Test
    public void simple() {
        assertEquals("foo", Id.generate("foo"));
    }

    @Test
    public void complex() {
        assertEquals("foo-bar-1-2-3", Id.generate("foo", "bar", "1-2", "3"));
    }

    @Test
    public void emptyNull() {
        assertEquals("foo-bar", Id.generate("foo", "", null, "", "", "bar", ""));
    }

    @Test(expected = IllegalArgumentException.class)
    public void illegal() {
        Id.generate(null);
    }
}