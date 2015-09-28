package org.jboss.hal.meta.description;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @author Harald Pehl
 */
public class ResourceDescriptionTest {

    private ResourceDescription resourceDescription;

    @Before
    public void setUp() {
        resourceDescription = RrdFactory.mailSessions();
    }

    @Test
    public void getChildResourceDescription() {
        ResourceDescription childDescription = resourceDescription.getChildDescription("custom");
        assertNotNull(childDescription);
        assertTrue(childDescription.hasAttributes());
    }

    @Test
    public void getChildInstanceDescription() {
        ResourceDescription childDescription = resourceDescription.getChildDescription("server", "smtp");
        assertNotNull(childDescription);
        assertTrue(childDescription.hasAttributes());
    }
}