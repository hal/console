package org.jboss.hal.meta.processing;

import java.util.Collections;

import com.google.common.collect.Sets;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.description.ResourceDescriptionRegistry;
import org.jboss.hal.meta.security.SecurityContextRegistry;
import org.junit.Before;
import org.junit.Test;

import static org.jboss.hal.meta.processing.LookupResult.ALL_PRESENT;
import static org.jboss.hal.meta.processing.LookupResult.NOTHING_PRESENT;
import static org.jboss.hal.meta.processing.LookupResult.RESOURCE_DESCRIPTION_PRESENT;
import static org.jboss.hal.meta.processing.LookupResult.SECURITY_CONTEXT_PRESENT;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Harald Pehl
 */
public class LookupTest {

    private ResourceDescriptionRegistry descriptionRegistry;
    private SecurityContextRegistry securityContextRegistry;
    private Lookup lookup;
    private AddressTemplate foo;

    @Before
    @SuppressWarnings("unchecked")
    public void setUp() {
        descriptionRegistry = mock(ResourceDescriptionRegistry.class);
        securityContextRegistry = mock(SecurityContextRegistry.class);
        lookup = new Lookup(securityContextRegistry, descriptionRegistry);
        foo = AddressTemplate.of("foo");
    }

    @Test
    public void noTemplates() {
        LookupResult lookupResult = lookup.check(Collections.emptySet(), false);
        assertTrue(lookupResult.templates().isEmpty());
    }

    @Test
    public void nothingPresent() {
        when(descriptionRegistry.contains(foo)).thenReturn(false);
        when(securityContextRegistry.contains(foo)).thenReturn(false);

        LookupResult lookupResult = lookup.check(Sets.newHashSet(foo), false);
        assertEquals(NOTHING_PRESENT, lookupResult.missingMetadata(foo));
    }

    @Test
    public void resourceDescriptionPresent() {
        when(descriptionRegistry.contains(foo)).thenReturn(true);
        when(securityContextRegistry.contains(foo)).thenReturn(false);

        LookupResult lookupResult = lookup.check(Sets.newHashSet(foo), false);
        assertEquals(RESOURCE_DESCRIPTION_PRESENT, lookupResult.missingMetadata(foo));
    }

    @Test
    public void securityContextPresent() {
        when(descriptionRegistry.contains(foo)).thenReturn(false);
        when(securityContextRegistry.contains(foo)).thenReturn(true);

        LookupResult lookupResult = lookup.check(Sets.newHashSet(foo), false);
        assertEquals(SECURITY_CONTEXT_PRESENT, lookupResult.missingMetadata(foo));
    }

    @Test
    public void allPresent() {
        when(descriptionRegistry.contains(foo)).thenReturn(true);
        when(securityContextRegistry.contains(foo)).thenReturn(true);

        LookupResult lookupResult = lookup.check(Sets.newHashSet(foo), false);
        assertEquals(ALL_PRESENT, lookupResult.missingMetadata(foo));
    }
}