package org.jboss.hal.core.mbui;

import com.google.common.collect.ImmutableSortedSet;
import org.jboss.hal.dmr.Property;
import org.jboss.hal.dmr.model.ResourceDescription;
import org.jboss.hal.security.SecurityContext;
import org.junit.Test;

import java.util.Iterator;

import static org.junit.Assert.assertEquals;

/**
 * @author Harald Pehl
 */
public class PropertyFilterTest {

    @Test
    public void allAttributes() {
        ResourceDescription resourceDescription = new ResourceDescriptionBuilder()
                .attributes("foo", "bar", "baz", "qux");
        ModelNodeForm.Builder builder = new ModelNodeForm.Builder("allAttributes", SecurityContext.RWX, resourceDescription);
        ImmutableSortedSet<Property> properties = new PropertyFilter(builder).filter();
        Iterator<Property> iterator = properties.iterator();

        assertEquals(4, properties.size());
        assertEquals("bar", iterator.next().getName());
        assertEquals("baz", iterator.next().getName());
        assertEquals("foo", iterator.next().getName());
        assertEquals("qux", iterator.next().getName());
    }

    @Test
    public void includes() {
        ResourceDescription resourceDescription = new ResourceDescriptionBuilder()
                .attributes("foo", "bar", "baz", "qux");
        ModelNodeForm.Builder builder = new ModelNodeForm.Builder("includes", SecurityContext.RWX, resourceDescription)
                .include("foo", "bar");
        ImmutableSortedSet<Property> properties = new PropertyFilter(builder).filter();

        assertEquals(2, properties.size());
        assertEquals("bar", properties.first().getName());
        assertEquals("foo", properties.last().getName());
    }

    @Test
    public void excludes() {
        ResourceDescription resourceDescription = new ResourceDescriptionBuilder()
                .attributes("foo", "bar", "baz", "qux");
        ModelNodeForm.Builder builder = new ModelNodeForm.Builder("includes", SecurityContext.RWX, resourceDescription)
                .exclude("foo", "bar");
        ImmutableSortedSet<Property> properties = new PropertyFilter(builder).filter();

        assertEquals(2, properties.size());
        assertEquals("baz", properties.first().getName());
        assertEquals("qux", properties.last().getName());
    }
}