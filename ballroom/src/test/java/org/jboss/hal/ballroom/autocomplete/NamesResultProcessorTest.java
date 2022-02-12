/*
 *  Copyright 2022 Red Hat
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.jboss.hal.ballroom.autocomplete;

import java.util.List;

import org.jboss.hal.dmr.ExternalModelNode;
import org.jboss.hal.dmr.ModelNode;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@SuppressWarnings({ "HardCodedStringLiteral", "DuplicateStringLiteralInspection" })
public class NamesResultProcessorTest {

    private static final String[] NAMES = new String[] {
            "management",
            "private",
            "public",
            "unsecure"
    };

    private ReadChildrenProcessor resultProcessor;
    private ModelNode nodes;

    @Before
    public void setUp() throws Exception {
        resultProcessor = new NamesResultProcessor();
        nodes = ExternalModelNode.read(NamesResultProcessorTest.class.getResourceAsStream("names.dmr"));
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
    }

    @Test
    public void oneMatch() throws Exception {
        List<ReadChildrenResult> results = resultProcessor.processToModel("g", nodes);
        assertEquals(1, results.size());
        assertEquals("management", results.get(0).name);
    }

    @Test
    public void twoMatches() throws Exception {
        List<ReadChildrenResult> results = resultProcessor.processToModel("p", nodes);
        assertEquals(2, results.size());
        assertEquals("private", results.get(0).name);
        assertEquals("public", results.get(1).name);
    }

    @Test
    public void noMatches() throws Exception {
        List<ReadChildrenResult> results = resultProcessor.processToModel("foo", nodes);
        assertTrue(results.isEmpty());
    }
}