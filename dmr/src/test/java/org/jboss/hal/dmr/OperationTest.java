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
package org.jboss.hal.dmr;

import java.util.Collections;

import org.junit.Test;

import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.junit.Assert.assertEquals;

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

        assertOperation(new Operation(modelNode));
    }

    @Test
    public void fromBuilder() throws Exception {
        ResourceAddress address = new ResourceAddress()
                .add("subsystem", "datasources")
                .add("data-source", "foo");

        Operation operation = new Operation.Builder(address, ADD)
                .param(JNDI_NAME, "java:/bar")
                .header("header1", "value1")
                .build();

        assertOperation(operation);
    }

    @Test
    public void runAs() throws Exception {
        ResourceAddress address = new ResourceAddress()
                .add("subsystem", "datasources")
                .add("data-source", "foo");

        Operation operation = new Operation.Builder(address, ADD)
                .param(JNDI_NAME, "java:/bar")
                .header("header1", "value1")
                .build();

        assertOperation(operation.runAs(Collections.singleton("Administrator")), "Administrator");
    }

    private void assertOperation(Operation operation) {
        assertOperation(operation, null);
    }

    private void assertOperation(Operation operation, String runAs) {
        assertEquals(ADD, operation.getName());
        assertEquals("/subsystem=datasources/data-source=foo", operation.getAddress().toString());

        ModelNode parameter = new ModelNode();
        parameter.get(JNDI_NAME).set("java:/bar");
        assertEquals(parameter, operation.getParameter());

        ModelNode header = new ModelNode();
        header.get("header1").set("value1");
        if (runAs != null) {
            header.get(ROLES).set(runAs);
            assertEquals(header, operation.getHeader());
        }

        StringBuilder expected = new StringBuilder();
        expected.append("/subsystem=datasources/data-source=foo:add(jndi-name=java:/bar){header1=value1");
        if (runAs != null) {
            expected.append(",roles=").append(runAs);
        }
        expected.append("}");
        assertEquals(expected.toString(), operation.asCli());
    }
}