package org.jboss.hal.dmr;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class ResourceAddressTest {

    @Test(expected = IllegalArgumentException.class)
    public void fromNull() {
        ResourceAddress.from(null);
    }

    @Test
    public void fromEmpty() {
        ResourceAddress address = ResourceAddress.from("");
        assertEquals(ResourceAddress.root(), address);
    }

    @Test
    public void fromRoot() {
        ResourceAddress address = ResourceAddress.from("/");
        assertEquals(ResourceAddress.root(), address);
    }

    @Test
    public void fromAddressWithSlash() {
        ResourceAddress address = ResourceAddress.from("/subsystem=ee");
        assertFalse(address.isEmpty());
        assertEquals(1, address.size());
        assertArrayEquals(new String[]{"subsystem", "ee"}, segments(address));
    }

    @Test
    public void fromAddressWithoutSlash() {
        ResourceAddress address = ResourceAddress.from("subsystem=ee");
        assertFalse(address.isEmpty());
        assertEquals(1, address.size());
        assertArrayEquals(new String[]{"subsystem", "ee"}, segments(address));
    }

    @Test
    public void fromAddress() {
        ResourceAddress address = ResourceAddress.from("subsystem=ee/context-service=default");
        assertFalse(address.isEmpty());
        assertEquals(2, address.size());
        assertArrayEquals(new String[]{"subsystem", "ee", "context-service", "default"}, segments(address));
    }

    @Test
    public void startsWith() {
        ResourceAddress address = ResourceAddress.from("/host=primary/server=server1");
        assertTrue(ResourceAddress.root().startsWith(ResourceAddress.root()));
        assertTrue(ResourceAddress.from("/host=primary/server=server1").startsWith(ResourceAddress.root()));
        assertTrue(ResourceAddress.from("/host=primary/server=server1").startsWith(address));
        assertTrue(ResourceAddress.from("/host=primary/server=server1/").startsWith(address));
        assertTrue(ResourceAddress.from("/host=primary/server=server1/subsystem=undertow").startsWith(address));
        assertFalse(ResourceAddress.root().startsWith(address));
        assertFalse(ResourceAddress.from("/host=primary").startsWith(address));
        assertFalse(ResourceAddress.from("/host=primary/server=server2").startsWith(address));
        assertFalse(ResourceAddress.from("/host=secondary/server=server1").startsWith(address));
        assertFalse(ResourceAddress.from("/subsystem=undertow").startsWith(address));
    }

    private String[] segments(ResourceAddress address) {
        List<String> segments = new ArrayList<>();
        for (Property property : address.asPropertyList()) {
            segments.add(property.getName());
            segments.add(property.getValue().asString());
        }
        return segments.toArray(new String[0]);
    }
}
