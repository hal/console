package org.jboss.hal.meta.processing;

import com.google.common.collect.Sets;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.MissingMetadataException;
import org.junit.Before;
import org.junit.Test;

import static org.jboss.hal.meta.processing.LookupResult.RESOURCE_DESCRIPTION_PRESENT;
import static org.jboss.hal.meta.processing.LookupResult.SECURITY_CONTEXT_PRESENT;
import static org.junit.Assert.assertEquals;

/**
 * @author Harald Pehl
 */
public class LookupResultTest {

    private LookupResult context;
    private AddressTemplate foo;

    @Before
    public void setUp() {
        foo = AddressTemplate.of("foo");
        context = new LookupResult("#token", Sets.newHashSet(foo), false);
    }

    @Test
    public void initialState() {
        assertEquals(1, context.templates().size());
        assertEquals(0, context.missingMetadata(foo));
    }

    @Test(expected = MissingMetadataException.class)
    public void missingMetadata() {
        context.missingMetadata(AddressTemplate.of("bar"));
    }

    @Test
    public void markResourceDescriptionPresent() {
        context.markMetadataPresent(foo, RESOURCE_DESCRIPTION_PRESENT);
        assertEquals(0b10, context.missingMetadata(foo));
    }

    @Test
    public void markSecurityContextPresent() {
        context.markMetadataPresent(foo, SECURITY_CONTEXT_PRESENT);
        assertEquals(0b01, context.missingMetadata(foo));
    }

    @Test
    public void markAllPresent() {
        context.markMetadataPresent(foo, RESOURCE_DESCRIPTION_PRESENT);
        context.markMetadataPresent(foo, SECURITY_CONTEXT_PRESENT);
        assertEquals(0b11, context.missingMetadata(foo));
    }
}