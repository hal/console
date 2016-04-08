/*
 * Copyright 2015-2016 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.hal.ballroom.typeahead;

import java.util.List;

import org.jboss.hal.ballroom.typeahead.GroupedResultProcessor.Grouped;
import org.jboss.hal.dmr.ExternalModelNode;
import org.jboss.hal.dmr.ModelDescriptionConstants;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.model.Operation;
import org.jboss.hal.dmr.model.ResourceAddress;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.StatementContext;
import org.junit.Before;
import org.junit.Test;

import static java.util.stream.Collectors.toList;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Harald Pehl
 */
@SuppressWarnings({"HardCodedStringLiteral", "DuplicateStringLiteralInspection"})
public class SingleOperationTwoWildcardsTest {

    private static final String[] NAMES = new String[]{
            // standard-sockets
            "ajp",
            "http",
            "https",
            "txn-recovery-environment",
            "txn-status-manager",
            // ha-sockets
            "ajp",
            "http",
            "https",
            "jgroups-mping",
            "jgroups-tcp",
            "jgroups-tcp-fd",
            "jgroups-udp",
            "jgroups-udp-fd",
            "modcluster",
            "txn-recovery-environment",
            "txn-status-manager",
            // full-sockets
            "ajp",
            "http",
            "https",
            "iiop",
            "iiop-ssl",
            "txn-recovery-environment",
            "txn-status-manager",
            // full-ha-sockets
            "ajp",
            "http",
            "https",
            "iiop",
            "iiop-ssl",
            "jgroups-mping",
            "jgroups-tcp",
            "jgroups-tcp-fd",
            "jgroups-udp",
            "jgroups-udp-fd",
            "modcluster",
            "txn-recovery-environment",
            "txn-status-manager",
    };


    private GroupedResultProcessor resultProcessor;
    private ModelNode result;

    @Before
    public void setUp() throws Exception {
        ResourceAddress address = AddressTemplate.of("/socket-binding-group=*/socket-binding=*")
                .resolve(StatementContext.NOOP);
        Operation operation = new Operation.Builder(ModelDescriptionConstants.READ_RESOURCE_OPERATION, address).build();
        resultProcessor = new GroupedResultProcessor(operation);
        result = ExternalModelNode
                .read(NamesResultProcessorTest.class.getResourceAsStream("single_operation_two_wildcards.dmr"));
    }

    @Test
    public void nullQuery() throws Exception {
        List<Grouped> models = resultProcessor.processToModel(null, result);
        assertTrue(models.isEmpty());
    }

    @Test
    public void emptyQuery() throws Exception {
        List<Grouped> models = resultProcessor.processToModel("", result);
        assertTrue(models.isEmpty());
    }

    @Test
    public void wildcardQuery() throws Exception {
        List<Grouped> models = resultProcessor.processToModel("*", result);
        List<String> names = models.stream().map(model -> model.name).collect(toList());
        assertArrayEquals(NAMES, names.toArray());
        models.forEach(model -> {
            assertEquals(1, model.groups.size());
            assertTrue(model.groups.get(0).startsWith("socket-binding-group => "));
        });
    }

    @Test
    public void oneMatch() throws Exception {
        List<Grouped> models = resultProcessor.processToModel("iiop-ssl", result);
        assertEquals(2, models.size());
        models.forEach(model -> assertEquals("iiop-ssl", model.name));
        assertEquals("socket-binding-group => full-sockets", models.get(0).groups.get(0));
        assertEquals("socket-binding-group => full-ha-sockets", models.get(1).groups.get(0));
    }

    @Test
    public void twoMatches() throws Exception {
    }

    @Test
    public void noMatches() throws Exception {
    }
}