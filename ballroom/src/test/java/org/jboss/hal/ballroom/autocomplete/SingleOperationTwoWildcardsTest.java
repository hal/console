/*
 * Copyright 2015-2016 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.hal.ballroom.autocomplete;

import java.util.List;
import java.util.Map;

import org.jboss.hal.dmr.ExternalModelNode;
import org.jboss.hal.dmr.ModelNode;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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


    private ReadChildrenProcessor resultProcessor;
    private ModelNode nodes;

    @Before
    public void setUp() throws Exception {
        resultProcessor = new SingleReadChildrenProcessor();
        nodes = ExternalModelNode
                .read(NamesResultProcessorTest.class.getResourceAsStream("single-operation-two-wildcards.dmr"));
    }

    @Test
    public void nullQuery() throws Exception {
        List<ReadChildrenResult> results = resultProcessor.processToModel(null, nodes);
        assertTrue(results.isEmpty());
    }

    @Test
    public void emptyQuery() throws Exception {
        List<ReadChildrenResult> results = resultProcessor.processToModel("", nodes);
        assertTrue(results.isEmpty());
    }

    @Test
    public void wildcardQuery() throws Exception {
        List<ReadChildrenResult> results = resultProcessor.processToModel("*", nodes);
        assertArrayEquals(NAMES, results.stream().map(result -> result.name).toArray(String[]::new));
        results.forEach(model -> {
            assertEquals(1, model.addresses.size());
            assertEquals("socket-binding-group", model.addresses.keySet().iterator().next());
        });
    }

    @Test
    public void oneMatch() throws Exception {
        List<ReadChildrenResult> results = resultProcessor.processToModel("iiop-ssl", nodes);
        assertEquals(2, results.size());
        results.forEach(model -> assertEquals("iiop-ssl", model.name));

        Map.Entry<String, String> entry = results.get(0).addresses.entrySet().iterator().next();
        assertEquals("socket-binding-group", entry.getKey());
        assertEquals("full-sockets", entry.getValue());

        entry = results.get(1).addresses.entrySet().iterator().next();
        assertEquals("socket-binding-group", entry.getKey());
        assertEquals("full-ha-sockets", entry.getValue());
    }

    @Test
    public void twoMatches() throws Exception {
    }

    @Test
    public void noMatches() throws Exception {
    }
}