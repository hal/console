package org.jboss.hal.meta.functions;

import com.google.common.collect.Sets;
import org.jboss.gwt.flow.Control;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.description.ResourceDescriptions;
import org.jboss.hal.meta.security.SecurityFramework;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;

import static org.jboss.hal.meta.functions.MetadataContext.*;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

/**
 * @author Harald Pehl
 */
public class LookupFunctionTest {

    private ResourceDescriptions descriptionRegistry;
    private SecurityFramework securityFramework;
    private Control<MetadataContext> control;
    private LookupFunction function;
    private AddressTemplate foo;

    @Before
    @SuppressWarnings("unchecked")
    public void setUp() {
        descriptionRegistry = mock(ResourceDescriptions.class);
        securityFramework = mock(SecurityFramework.class);
        control = mock(Control.class);
        function = new LookupFunction(descriptionRegistry, securityFramework);
        foo = AddressTemplate.of("foo");
    }

    @Test
    public void noTemplates() {
        when(control.getContext()).thenReturn(new MetadataContext(Collections.<AddressTemplate>emptySet(), false));
        function.execute(control);
        verify(control).proceed();
    }

    @Test
    public void nothingPresent() {
        when(control.getContext()).thenReturn(new MetadataContext(Sets.newHashSet(foo), false));
        when(descriptionRegistry.contains(foo)).thenReturn(false);
        when(securityFramework.contains(foo)).thenReturn(false);

        function.execute(control);

        verify(control).proceed();
        assertEquals(NOTHING_PRESENT, control.getContext().missingMetadata(foo));
    }

    @Test
    public void resourceDescriptionPresent() {
        when(control.getContext()).thenReturn(new MetadataContext(Sets.newHashSet(foo), false));
        when(descriptionRegistry.contains(foo)).thenReturn(true);
        when(securityFramework.contains(foo)).thenReturn(false);

        function.execute(control);

        verify(control).proceed();
        assertEquals(RESOURCE_DESCRIPTION_PRESENT, control.getContext().missingMetadata(foo));
    }

    @Test
    public void securityContextPresent() {
        when(control.getContext()).thenReturn(new MetadataContext(Sets.newHashSet(foo), false));
        when(descriptionRegistry.contains(foo)).thenReturn(false);
        when(securityFramework.contains(foo)).thenReturn(true);

        function.execute(control);

        verify(control).proceed();
        assertEquals(SECURITY_CONTEXT_PRESENT, control.getContext().missingMetadata(foo));
    }

    @Test
    public void allPresent() {
        when(control.getContext()).thenReturn(new MetadataContext(Sets.newHashSet(foo), false));
        when(descriptionRegistry.contains(foo)).thenReturn(true);
        when(securityFramework.contains(foo)).thenReturn(true);

        function.execute(control);

        verify(control).proceed();
        assertEquals(ALL_PRESENT, control.getContext().missingMetadata(foo));
    }
}