package org.jboss.hal.meta.functions;

import com.google.common.collect.Sets;
import org.jboss.gwt.flow.Control;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.EchoContext;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.meta.dmr.Operation;
import org.jboss.hal.meta.dmr.ResourceAddress;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.meta.functions.MetadataContext.RESOURCE_DESCRIPTION_PRESENT;
import static org.jboss.hal.meta.functions.MetadataContext.SECURITY_CONTEXT_PRESENT;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * @author Harald Pehl
 */
public class CreateRrdOpFunctionTest {

    private Control<MetadataContext> control;
    private CreateRrdOpFunction function;
    private StatementContext statementContext;

    @Before
    @SuppressWarnings("unchecked")
    public void setUp() {
        control = mock(Control.class);
        statementContext = new EchoContext();
        function = new CreateRrdOpFunction(statementContext);
    }

    @Test
    public void noTemplates() {
        when(control.getContext()).thenReturn(new MetadataContext(Collections.<AddressTemplate>emptySet(), false));
        function.execute(control);
        verify(control).proceed();

        List<Operation> operations = control.getContext().pop();
        assertTrue(operations.isEmpty());
    }

    @Test
    public void mixed() {
        AddressTemplate nothingPresent = AddressTemplate.of("nothing=present");
        AddressTemplate descriptionPresent = AddressTemplate.of("description=present");
        AddressTemplate securityContextPresent = AddressTemplate.of("securityContext=present");
        AddressTemplate allPresent = AddressTemplate.of("all=present");

        MetadataContext metadataContext = new MetadataContext(
                Sets.newHashSet(nothingPresent, descriptionPresent, securityContextPresent, allPresent), false);

        metadataContext.markMetadataPresent(descriptionPresent, RESOURCE_DESCRIPTION_PRESENT);
        metadataContext.markMetadataPresent(securityContextPresent, SECURITY_CONTEXT_PRESENT);
        metadataContext.markMetadataPresent(allPresent, RESOURCE_DESCRIPTION_PRESENT);
        metadataContext.markMetadataPresent(allPresent, SECURITY_CONTEXT_PRESENT);

        when(control.getContext()).thenReturn(metadataContext);
        function.execute(control);
        verify(control).proceed();

        List<Operation> operations = control.getContext().pop();
        assertEquals(3, operations.size());

        Operation operation = findOperation(operations, nothingPresent);
        assertEquals(COMBINED_DESCRIPTIONS, operation.get(ACCESS_CONTROL).asString());
        assertEquals(true, operation.get(OPERATIONS).asBoolean());

        operation = findOperation(operations, descriptionPresent);
        assertEquals("trim-descriptions", operation.get(ACCESS_CONTROL).asString());
        assertEquals(true, operation.get(OPERATIONS).asBoolean());

        operation = findOperation(operations, securityContextPresent);
        assertFalse(operation.get(ACCESS_CONTROL).isDefined());
        assertFalse(operation.get(OPERATIONS).isDefined());
    }

    private Operation findOperation(List<Operation> operations, AddressTemplate template) {
        ResourceAddress address = template.resolve(statementContext);
        for (Operation operation : operations) {
            if (operation.get(ADDRESS).equals(address)) {
                return operation;
            }
        }
        throw new IllegalStateException("No operation found for " + template);
    }

    @Test
    public void recursive() {
        when(control.getContext())
                .thenReturn(new MetadataContext(Sets.newHashSet(AddressTemplate.of("foo=bar")), true));
        function.execute(control);
        verify(control).proceed();

        List<Operation> operations = control.getContext().pop();
        Operation operation = operations.get(0);
        assertTrue(operation.get(RECURSIVE).asBoolean());
    }
}
