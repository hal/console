package org.jboss.hal.meta;


import java.util.List;

import org.jboss.hal.dmr.ModelNodeHelper;
import org.jboss.hal.dmr.Property;
import org.jboss.hal.dmr.ResourceAddress;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@SuppressWarnings("HardCodedStringLiteral")
public class AddressTemplateTest {

    @Test
    public void root() {
        AddressTemplate at = AddressTemplate.ROOT;
        assertTrue(at.getTemplate().length() == 0);
    }

    @Test
    public void empty() {
        AddressTemplate at = AddressTemplate.of("");
        assertTrue(at.getTemplate().length() == 0);
    }

    @Test(expected = AssertionError.class)
    public void nil() {
        AddressTemplate.of((String) null);
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

        at = AddressTemplate.of("opt://a=b");
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
    public void firstKey() {
        AddressTemplate at = AddressTemplate.ROOT;
        assertNull(at.firstName());

        at = AddressTemplate.of("a=b");
        assertEquals("a", at.firstName());

        at = AddressTemplate.of("a=b/{c}");
        assertEquals("a", at.firstName());
    }

    @Test
    public void firstValue() {
        AddressTemplate at = AddressTemplate.ROOT;
        assertNull(at.firstValue());

        at = AddressTemplate.of("a=b");
        assertEquals("b", at.firstValue());

        at = AddressTemplate.of("a=b/{c}");
        assertEquals("b", at.firstValue());
    }

    @Test
    public void lastKey() {
        AddressTemplate at = AddressTemplate.ROOT;
        assertNull(at.lastName());

        at = AddressTemplate.of("a=b");
        assertEquals("a", at.lastName());

        at = AddressTemplate.of("a=b/{c}");
        assertNull(at.lastName());

        at = AddressTemplate.of("{a}/b={c}");
        assertEquals("b", at.lastName());
    }

    @Test
    public void lastValue() {
        AddressTemplate at = AddressTemplate.ROOT;
        assertNull(at.lastValue());

        at = AddressTemplate.of("a=b");
        assertEquals("b", at.lastValue());

        at = AddressTemplate.of("a=b/{c}");
        assertNull(at.lastValue());

        at = AddressTemplate.of("{a}/b={c}");
        assertEquals("{c}", at.lastValue());
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
    public void parentOfRoot() {
        AddressTemplate at = AddressTemplate.of("/");
        assertEquals(AddressTemplate.of("/"), at.getParent());
    }

    @Test
    public void parentOfFirstLevel() {
        AddressTemplate at = AddressTemplate.of("/a=b");
        assertEquals(AddressTemplate.of("/"), at.getParent());
    }

    @Test
    public void parent() {
        AddressTemplate at = AddressTemplate.of("{a}/b=c/{d}=e/f=g"); // 4 tokens
        assertEquals(AddressTemplate.of("{a}/b=c/{d}=e"), at.getParent());
        assertEquals(AddressTemplate.of("{a}/b=c"), at.getParent().getParent());
        assertEquals(AddressTemplate.of("{a}"), at.getParent().getParent().getParent());
        assertEquals(AddressTemplate.of("/"), at.getParent().getParent().getParent().getParent());
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
        at = at.replaceWildcards(null, (String[]) null);
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
        at = at.replaceWildcards("b", (String[]) null);
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
        ResourceAddress resolved = at.resolve(StatementContext.NOOP);
        assertResolved(new String[][]{{"a", "a"}, {"b", "c"}}, resolved);
    }

    @Test
    public void resolveWildcards() {
        AddressTemplate at = AddressTemplate.of("a=*/c=*");
        ResourceAddress resolved = at.resolve(StatementContext.NOOP);
        assertResolved(new String[][]{{"a", "*"}, {"c", "*"}}, resolved);

        resolved = at.resolve(StatementContext.NOOP, (String[]) null);
        assertResolved(new String[][]{{"a", "*"}, {"c", "*"}}, resolved);

        resolved = at.resolve(StatementContext.NOOP, "b");
        assertResolved(new String[][]{{"a", "b"}, {"c", "*"}}, resolved);

        resolved = at.resolve(StatementContext.NOOP, "b", "d");
        assertResolved(new String[][]{{"a", "b"}, {"c", "d"}}, resolved);

        resolved = at.resolve(StatementContext.NOOP, "b", "d", "foo");
        assertResolved(new String[][]{{"a", "b"}, {"c", "d"}}, resolved);
    }

    @Test
    public void slashes() {
        AddressTemplate at = AddressTemplate.of("a=b/" + ModelNodeHelper.encodeValue("c=/") + "/d=e");
        assertEquals("a=b/c=%2F/d=e", at.getTemplate());

        at = AddressTemplate.of("a=b")
                .append(ModelNodeHelper.encodeValue("c=/"))
                .append("d=e");
        assertEquals("a=b/c=%2F/d=e", at.getTemplate());
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