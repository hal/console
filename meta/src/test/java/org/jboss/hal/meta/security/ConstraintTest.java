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
package org.jboss.hal.meta.security;

import org.jboss.hal.meta.AddressTemplate;
import org.junit.Before;
import org.junit.Test;

import static org.jboss.hal.dmr.ModelDescriptionConstants.ADD;
import static org.jboss.hal.dmr.ModelDescriptionConstants.ENABLED;
import static org.jboss.hal.meta.security.Permission.EXECUTABLE;
import static org.jboss.hal.meta.security.Permission.WRITABLE;
import static org.jboss.hal.meta.security.Target.ATTRIBUTE;
import static org.jboss.hal.meta.security.Target.OPERATION;
import static org.junit.Assert.assertEquals;

@SuppressWarnings({"HardCodedStringLiteral", "DuplicateStringLiteralInspection"})
public class ConstraintTest {

    private AddressTemplate template;

    @Before
    public void setUp() throws Exception {
        template = AddressTemplate.of("{selected.profile}/subsystem=datasources/data-source=*");
    }

    @Test(expected = IllegalArgumentException.class)
    public void parseNull() throws Exception {
        Constraint.parse(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void parseEmpty() throws Exception {
        Constraint.parse("");
    }

    @Test(expected = IllegalArgumentException.class)
    public void parseBlank() throws Exception {
        Constraint.parse("   ");
    }

    @Test(expected = IllegalArgumentException.class)
    public void parseInvalid() throws Exception {
        Constraint.parse("foo");
    }

    @Test
    public void parseOperation() throws Exception {
        String input = "executable({selected.profile}/subsystem=datasources/data-source=*:add)";
        Constraint constraint = Constraint.parse(input);

        assertEquals(Constraint.executable(template, ADD), constraint);
    }

    @Test
    public void parseAttribute() throws Exception {
        String input = "writable({selected.profile}/subsystem=datasources/data-source=*@enabled)";
        Constraint constraint = Constraint.parse(input);

        assertEquals(Constraint.writable(template, ENABLED), constraint);
    }

    @Test
    public void executable() throws Exception {
        Constraint constraint = Constraint.executable(template, ADD);

        assertEquals(template, constraint.getTemplate());
        assertEquals(ADD, constraint.getName());
        assertEquals(OPERATION, constraint.getTarget());
        assertEquals(EXECUTABLE, constraint.getPermission());
    }

    @Test
    public void writable() throws Exception {
        Constraint constraint = Constraint.writable(template, ENABLED);

        assertEquals(template, constraint.getTemplate());
        assertEquals(ENABLED, constraint.getName());
        assertEquals(ATTRIBUTE, constraint.getTarget());
        assertEquals(WRITABLE, constraint.getPermission());
    }

    @Test
    public void operationData() throws Exception {
        assertEquals("executable({selected.profile}/subsystem=datasources/data-source=*:add)",
                Constraint.executable(template, ADD).data());
    }

    @Test
    public void attributeData() throws Exception {
        assertEquals("writable({selected.profile}/subsystem=datasources/data-source=*@enabled)",
                Constraint.writable(template, ENABLED).data());
    }

    @Test
    public void operationRoundTrip() throws Exception {
        Constraint constraint1 = Constraint.executable(template, ADD);
        String data = constraint1.data();
        Constraint constraint2 = Constraint.parse(data);

        assertEquals(constraint1, constraint2);
    }

    @Test
    public void attributeRoundTrip() throws Exception {
        Constraint constraint1 = Constraint.writable(template, ENABLED);
        String data = constraint1.data();
        Constraint constraint2 = Constraint.parse(data);

        assertEquals(constraint1, constraint2);
    }
}