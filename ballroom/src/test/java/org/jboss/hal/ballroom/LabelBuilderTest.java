package org.jboss.hal.ballroom;

import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Property;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Attributes taken from /subsystem=datasources/data-source=*:read-resource-description
 * @author Harald Pehl
 */
@SuppressWarnings({"HardCodedStringLiteral", "DuplicateStringLiteralInspection"})
public class LabelBuilderTest {

    private LabelBuilder builder;

    @Before
    public void setUp() {
        builder = new LabelBuilder();
    }

    @Test
    public void capitalize() {
        assertEquals("Background Validation", builder.label(property("background-validation")));
        assertEquals("Enabled", builder.label(property("enabled")));
    }

    @Test
    public void specials() {
        assertEquals("Check Valid Connection SQL", builder.label(property("check-valid-connection-sql")));
        assertEquals("Connection URL", builder.label(property("connection-url")));
        assertEquals("JNDI Name", builder.label(property("jndi-name")));
        assertEquals("URL Selector Strategy Class Name", builder.label(property("url-selector-strategy-class-name")));
        assertEquals("Modify WSDL Address", builder.label(property("modify-wsdl-address")));
        assertEquals("WSDL Port", builder.label(property("wsdl-port")));
    }

    private Property property(String name) {
        return new Property(name, new ModelNode());
    }
}