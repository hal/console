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

import org.jboss.hal.dmr.ExternalModelNode;
import org.jboss.hal.dmr.ModelNode;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

@SuppressWarnings({"HardCodedStringLiteral", "DuplicateStringLiteralInspection"})
public class SingleOperationThreeWildcardsTest {

    private ReadChildrenProcessor resultProcessor;
    private ModelNode nodes;

    @Before
    public void setUp() throws Exception {
        resultProcessor = new SingleReadChildrenProcessor();
        nodes = ExternalModelNode
                .read(NamesResultProcessorTest.class.getResourceAsStream("single-operation-three-wildcards.dmr"));
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
    }

    @Test
    public void oneMatch() throws Exception {
    }

    @Test
    public void twoMatches() throws Exception {
    }

    @Test
    public void noMatches() throws Exception {
    }
}