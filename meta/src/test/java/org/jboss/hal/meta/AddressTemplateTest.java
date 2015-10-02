package org.jboss.hal.meta;


import org.jboss.hal.dmr.Property;
import org.jboss.hal.dmr.model.ResourceAddress;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

/**
 * @author Harald Pehl
 */
public class AddressTemplateTest {

    @Test
    public void root() {
        AddressTemplate at = AddressTemplate.of("/");
        assertTrue(at.getTemplate().length() == 0);
    }

    @Test
    public void empty() {
        AddressTemplate at = AddressTemplate.of("");
        assertTrue(at.getTemplate().length() == 0);
    }

    @Test(expected = AssertionError.class)
    public void nil() {
        AddressTemplate.of(null);
    }

    @Test
    public void absolute() {
        AddressTemplate at = AddressTemplate.of("/a=b/c=d");
        assertEquals("a=b/c=d", at.getTemplate());
    }

    @Test
    public void relative() {
        AddressTemplate at = AddressTemplate.of("a=b/c=d");
        assertEquals("a=b/c=d", at.getTemplate());
    }

    @Test
    public void optional() {
        AddressTemplate at = AddressTemplate.of("a=b");
        assertFalse(at.isOptional());

        at = AddressTemplate.of("opt:/a=b");
        assertTrue(at.isOptional());
    }

    @Test
    public void tuple() {
        AddressTemplate at = AddressTemplate.of("{a}/b=c");
        assertEquals("{a}/b=c", at.getTemplate());
    }

    @Test
    public void key() {
        AddressTemplate at = AddressTemplate.of("a=b/b={c}");
        assertEquals("a=b/b={c}", at.getTemplate());
    }

    @Test
    public void resourceType() {
        AddressTemplate at = AddressTemplate.of("/");
        assertNull(at.getResourceType());

        at = AddressTemplate.of("a=b");
        assertEquals("a", at.getResourceType());

        at = AddressTemplate.of("a=b/{c}");
        assertNull(at.getResourceType());

        at = AddressTemplate.of("{a}/b={c}");
        assertEquals("b", at.getResourceType());
    }

    @Test
    public void append() {
        AddressTemplate at = AddressTemplate.of("a=b");
        at = at.append("c=d");
        assertEquals("a=b/c=d", at.getTemplate());

        at = AddressTemplate.of("a=b");
        at = at.append("/c=d");
        assertEquals("a=b/c=d", at.getTemplate());
    }

    @Test
    public void appendTuple() {
        AddressTemplate at = AddressTemplate.of("a=b");
        at = at.append("{c}");
        assertEquals("a=b/{c}", at.getTemplate());
    }

    @Test
    public void appendKey() {
        AddressTemplate at = AddressTemplate.of("a=b");
        at = at.append("c={d}");
        assertEquals("a=b/c={d}", at.getTemplate());
    }

    @Test
    public void subTemplate() {
        AddressTemplate at = AddressTemplate.of("{a}/b=c/{d}=e/f=g"); // 4 tokens

        assertEquals("", at.subTemplate(0, 0).getTemplate());
        assertEquals("", at.subTemplate(2, 2).getTemplate());
        assertEquals("b=c", at.subTemplate(1, 2).getTemplate());
        assertEquals("{d}=e/f=g", at.subTemplate(2, 4).getTemplate());
        assertEquals(at, at.subTemplate(0, 4));
    }

    @Test
    public void replaceWildcards() {
        AddressTemplate at = AddressTemplate.of("a=b");
        at = at.replaceWildcards(null);
        assertEquals("a=b", at.getTemplate());

        at = AddressTemplate.of("a=b");
        at = at.replaceWildcards(null, (String[])null);
        assertEquals("a=b", at.getTemplate());

        at = AddressTemplate.of("a=b");
        at = at.replaceWildcards("foo");
        assertEquals("a=b", at.getTemplate());

        at = AddressTemplate.of("{a}/b={c}");
        at = at.replaceWildcards("foo");
        assertEquals("{a}/b={c}", at.getTemplate());

        at = AddressTemplate.of("a=*/c=*");
        at = at.replaceWildcards("b");
        assertEquals("a=b/c=*", at.getTemplate());

        at = AddressTemplate.of("a=*/c=*");
        at = at.replaceWildcards("b", (String[])null);
        assertEquals("a=b/c=*", at.getTemplate());

        at = AddressTemplate.of("a=*/c=*");
        at = at.replaceWildcards("b", "d");
        assertEquals("a=b/c=d", at.getTemplate());

        at = AddressTemplate.of("a=*/c=*");
        at = at.replaceWildcards("b", "d", "foo");
        assertEquals("a=b/c=d", at.getTemplate());

        at = AddressTemplate.of("a=*/c={d}");
        at = at.replaceWildcards("b", "d", "foo");
        assertEquals("a=b/c={d}", at.getTemplate());
    }

    @Test
    public void resolve() {
        AddressTemplate at = AddressTemplate.of("{a}/b={c}");
        ResourceAddress resolved = at.resolve(new EchoContext());
        assertResolved(new String[][]{{"a", "a"}, {"b", "c"}}, resolved);
    }

    @Test
    public void resolveWildcards() {
        AddressTemplate at = AddressTemplate.of("a=*/c=*");
        ResourceAddress resolved = at.resolve(new EchoContext());
        assertResolved(new String[][]{{"a", "*"}, {"c", "*"}}, resolved);

        resolved = at.resolve(new EchoContext(), (String[])null);
        assertResolved(new String[][]{{"a", "*"}, {"c", "*"}}, resolved);

        resolved = at.resolve(new EchoContext(), "b");
        assertResolved(new String[][]{{"a", "b"}, {"c", "*"}}, resolved);

        resolved = at.resolve(new EchoContext(), "b", "d");
        assertResolved(new String[][]{{"a", "b"}, {"c", "d"}}, resolved);

        resolved = at.resolve(new EchoContext(), "b", "d", "foo");
        assertResolved(new String[][]{{"a", "b"}, {"c", "d"}}, resolved);
    }

    private void assertResolved(String[][] tuples, ResourceAddress resourceAddress) {
        List<Property> properties = resourceAddress.asPropertyList();
        assertEquals(tuples.length, properties.size());
        int i = 0;
        for (Property property : properties) {
            assertEquals(tuples[i][0], property.getName());
            assertEquals(tuples[i][1], property.getValue().asString());
            i++;
        }
    }
}