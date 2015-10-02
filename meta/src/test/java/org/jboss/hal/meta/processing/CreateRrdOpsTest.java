package org.jboss.hal.meta.processing;

import com.google.common.collect.Sets;
import org.jboss.hal.dmr.model.Operation;
import org.jboss.hal.dmr.model.ResourceAddress;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.EchoContext;
import org.jboss.hal.meta.StatementContext;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.meta.processing.LookupResult.RESOURCE_DESCRIPTION_PRESENT;
import static org.jboss.hal.meta.processing.LookupResult.SECURITY_CONTEXT_PRESENT;
import static org.junit.Assert.*;

/**
 * @author Harald Pehl
 */
public class CreateRrdOpsTest {

    private CreateRrdOps rrdOps;
    private StatementContext statementContext;

    @Before
    @SuppressWarnings("unchecked")
    public void setUp() {
        statementContext = new EchoContext();
        rrdOps = new CreateRrdOps(statementContext);
    }

    @Test
    public void noTemplates() {
        List<Operation> operations = rrdOps.create(new LookupResult(Collections.<AddressTemplate>emptySet(), false));
        assertTrue(operations.isEmpty());
    }

    @Test
    public void mixed() {
        AddressTemplate nothingPresent = AddressTemplate.of("nothing=present");
        AddressTemplate descriptionPresent = AddressTemplate.of("description=present");
        AddressTemplate securityContextPresent = AddressTemplate.of("securityContext=present");
        AddressTemplate allPresent = AddressTemplate.of("all=present");

        LookupResult lookupResult = new LookupResult(
                Sets.newHashSet(nothingPresent, descriptionPresent, securityContextPresent, allPresent), false);

        lookupResult.markMetadataPresent(descriptionPresent, RESOURCE_DESCRIPTION_PRESENT);
        lookupResult.markMetadataPresent(securityContextPresent, SECURITY_CONTEXT_PRESENT);
        lookupResult.markMetadataPresent(allPresent, RESOURCE_DESCRIPTION_PRESENT);
        lookupResult.markMetadataPresent(allPresent, SECURITY_CONTEXT_PRESENT);

        List<Operation> operations = rrdOps.create(lookupResult);
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
        List<Operation> operations = rrdOps.create(new LookupResult(Sets.newHashSet(AddressTemplate.of("foo=bar")), true));
        Operation operation = operations.get(0);
        assertTrue(operation.get(RECURSIVE).asBoolean());
    }
}
