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
import org.jboss.hal.dmr.model.Composite;
import org.jboss.hal.dmr.model.Operation;
import org.jboss.hal.dmr.model.ResourceAddress;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.StatementContext;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * @author Harald Pehl
 */
@SuppressWarnings({"HardCodedStringLiteral", "DuplicateStringLiteralInspection"})
public class CompositeOperationOneWildcardTest {

    private GroupedResultProcessor resultProcessor;
    private ModelNode result;

    @Before
    public void setUp() throws Exception {
        ResourceAddress address1 = AddressTemplate.of("/profile=full/subsystem=datasources/data-source=*")
                .resolve(StatementContext.NOOP);
        Operation operation1 = new Operation.Builder(ModelDescriptionConstants.READ_RESOURCE_OPERATION, address1)
                .build();
        ResourceAddress address2 = AddressTemplate.of("/profile=ha/subsystem=datasources/data-source=*")
                .resolve(StatementContext.NOOP);
        Operation operation2 = new Operation.Builder(ModelDescriptionConstants.READ_RESOURCE_OPERATION, address2)
                .build();
        resultProcessor = new GroupedResultProcessor(new Composite(operation1, operation2));
        result = ExternalModelNode
                .read(NamesResultProcessorTest.class.getResourceAsStream("composite_operation_one_wildcard.dmr"));
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