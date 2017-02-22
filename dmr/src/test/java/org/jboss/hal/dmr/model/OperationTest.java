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
package org.jboss.hal.dmr.model;

import org.jboss.hal.dmr.ModelNode;
import org.junit.Test;

import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.junit.Assert.assertEquals;

/**
 * @author Harald Pehl
 */
@SuppressWarnings({"HardCodedStringLiteral", "DuplicateStringLiteralInspection"})
public class OperationTest {

    @Test
    public void fromModelNode() throws Exception {
        ModelNode address = new ModelNode();
        address.add().set("subsystem", "datasources");
        address.add().set("data-source", "foo");

        ModelNode modelNode = new ModelNode();
        modelNode.get(OP).set(ADD);
        modelNode.get(ADDRESS).set(address);
        modelNode.get(JNDI_NAME).set("java:/bar");
        modelNode.get(OPERATION_HEADERS).get("header1").set("value1");
        modelNode.get(OPERATION_HEADERS).get(ROLES).set("Administrator");

        assertOperation(new Operation(modelNode));
    }

    @Test
    public void fromBuilder() throws Exception {
        Operation operation = new Operation.Builder(ADD,
                new ResourceAddress().add("subsystem", "datasources").add("data-source", "foo"))
                .param(JNDI_NAME, "java:/bar")
                .header("header1", "value1")
                .runAs("Administrator")
                .build();
        assertOperation(operation);
    }

    private void assertOperation(Operation operation) {
        assertEquals(ADD, operation.getName());
        assertEquals("/subsystem=datasources/data-source=foo", operation.getAddress().toString());

        ModelNode parameter = new ModelNode();
        parameter.get(JNDI_NAME).set("java:/bar");
        assertEquals(parameter, operation.getParameter());

        ModelNode header = new ModelNode();
        header.get("header1").set("value1");
        header.get(ROLES).set("Administrator");
        assertEquals(header, operation.getHeader());

        assertEquals("Administrator", operation.getRoles().get(0));

        assertEquals(
                "/subsystem=datasources/data-source=foo:add(jndi-name=java:/bar){header1=value1,roles=Administrator}",
                operation.asCli());
    }
}