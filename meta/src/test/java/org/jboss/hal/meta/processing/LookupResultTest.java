package org.jboss.hal.meta.processing;

import com.google.common.collect.Sets;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.MissingMetadataException;
import org.junit.Before;
import org.junit.Test;

import static org.jboss.hal.meta.processing.LookupResult.*;
import static org.junit.Assert.*;

/**
 * @author Harald Pehl
 */
public class LookupResultTest {

    private LookupResult lookupResult;
    private AddressTemplate foo;

    @Before
    public void setUp() {
        foo = AddressTemplate.of("foo");
        lookupResult = new LookupResult("#token", Sets.newHashSet(foo), false);
    }

    @Test
    public void initialState() {
        assertEquals(1, lookupResult.templates().size());
        assertEquals(0, lookupResult.missingMetadata(foo));
    }

    @Test(expected = MissingMetadataException.class)
    public void missingMetadata() {
        lookupResult.missingMetadata(AddressTemplate.of("bar"));
    }

    @Test
    public void markResourceDescriptionPresent() {
        lookupResult.markMetadataPresent(foo, RESOURCE_DESCRIPTION_PRESENT);
        assertEquals(0b10, lookupResult.missingMetadata(foo));
    }

    @Test
    public void markSecurityContextPresent() {
        lookupResult.markMetadataPresent(foo, SECURITY_CONTEXT_PRESENT);
        assertEquals(0b01, lookupResult.missingMetadata(foo));
    }

    @Test
    public void markAllPresent() {
        lookupResult.markMetadataPresent(foo, RESOURCE_DESCRIPTION_PRESENT);
        lookupResult.markMetadataPresent(foo, SECURITY_CONTEXT_PRESENT);
        assertEquals(0b11, lookupResult.missingMetadata(foo));
    }

    @Test
    public void allPresent() {
        final LookupResult localLookupResult = new LookupResult("#token",
                Sets.newHashSet(
                        AddressTemplate.of("one"),
                        AddressTemplate.of("two"),
                        AddressTemplate.of("three")),
                false);

        localLookupResult.markMetadataPresent(AddressTemplate.of("one"), ALL_PRESENT);
        localLookupResult.markMetadataPresent(AddressTemplate.of("two"), RESOURCE_DESCRIPTION_PRESENT);
        localLookupResult.markMetadataPresent(AddressTemplate.of("three"), SECURITY_CONTEXT_PRESENT);
        assertFalse(localLookupResult.allPresent());

        localLookupResult.markMetadataPresent(AddressTemplate.of("one"), ALL_PRESENT);
        localLookupResult.markMetadataPresent(AddressTemplate.of("two"), ALL_PRESENT);
        localLookupResult.markMetadataPresent(AddressTemplate.of("three"), ALL_PRESENT);
        assertTrue(localLookupResult.allPresent());
    }
}