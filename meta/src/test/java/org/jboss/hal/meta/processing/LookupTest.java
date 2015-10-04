package org.jboss.hal.meta.processing;

import com.google.common.collect.Sets;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.description.ResourceDescriptions;
import org.jboss.hal.meta.security.SecurityFramework;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;

import static org.jboss.hal.meta.processing.LookupResult.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Harald Pehl
 */
public class LookupTest {

    private ResourceDescriptions descriptionRegistry;
    private SecurityFramework securityFramework;
    private Lookup lookup;
    private AddressTemplate foo;

    @Before
    @SuppressWarnings("unchecked")
    public void setUp() {
        descriptionRegistry = mock(ResourceDescriptions.class);
        securityFramework = mock(SecurityFramework.class);
        lookup = new Lookup(descriptionRegistry, securityFramework);
        foo = AddressTemplate.of("foo");
    }

    @Test
    public void noTemplates() {
        LookupResult lookupResult = lookup.check("#token", Collections.<AddressTemplate>emptySet(), false);
        assertTrue(lookupResult.templates().isEmpty());
    }

    @Test
    public void nothingPresent() {
        when(descriptionRegistry.contains(foo)).thenReturn(false);
        when(securityFramework.contains(foo)).thenReturn(false);

        LookupResult lookupResult = lookup.check("#token", Sets.newHashSet(foo), false);
        assertEquals(NOTHING_PRESENT, lookupResult.missingMetadata(foo));
    }

    @Test
    public void resourceDescriptionPresent() {
        when(descriptionRegistry.contains(foo)).thenReturn(true);
        when(securityFramework.contains(foo)).thenReturn(false);

        LookupResult lookupResult = lookup.check("#token", Sets.newHashSet(foo), false);
        assertEquals(RESOURCE_DESCRIPTION_PRESENT, lookupResult.missingMetadata(foo));
    }

    @Test
    public void securityContextPresent() {
        when(descriptionRegistry.contains(foo)).thenReturn(false);
        when(securityFramework.contains(foo)).thenReturn(true);

        LookupResult lookupResult = lookup.check("#token", Sets.newHashSet(foo), false);
        assertEquals(SECURITY_CONTEXT_PRESENT, lookupResult.missingMetadata(foo));
    }

    @Test
    public void allPresent() {
        when(descriptionRegistry.contains(foo)).thenReturn(true);
        when(securityFramework.contains(foo)).thenReturn(true);

        LookupResult lookupResult = lookup.check("#token", Sets.newHashSet(foo), false);
        assertEquals(ALL_PRESENT, lookupResult.missingMetadata(foo));
    }
}