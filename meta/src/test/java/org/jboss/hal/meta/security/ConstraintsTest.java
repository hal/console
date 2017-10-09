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
import static org.jboss.hal.meta.security.Constraints.Operator.AND;
import static org.jboss.hal.meta.security.Constraints.Operator.OR;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@SuppressWarnings({"HardCodedStringLiteral", "DuplicateStringLiteralInspection"})
public class ConstraintsTest {

    private AddressTemplate template;

    @Before
    public void setUp() throws Exception {
        template = AddressTemplate.of("{selected.profile}/subsystem=datasources/data-source=*");
    }

    @Test
    public void parseNull() throws Exception {
        assertTrue(Constraints.parse(null).isEmpty());
    }

    @Test
    public void parseEmpty() throws Exception {
        assertTrue(Constraints.parse("").isEmpty());
    }

    @Test
    public void parseBlank() throws Exception {
        assertTrue(Constraints.parse("   ").isEmpty());
    }

    @Test
    public void parseInvalid() throws Exception {
        assertTrue(Constraints.parse("foo").isEmpty());
    }

    @Test
    public void parseOne() throws Exception {
        String input = "executable({selected.profile}/subsystem=datasources/data-source=*:add)";
        Constraints constraints = Constraints.parse(input);

        assertEquals(1, constraints.size());
        assertTrue(constraints.contains(Constraint.executable(template, ADD)));
    }

    @Test
    public void parseAnd() throws Exception {
        String input = "executable({selected.profile}/subsystem=datasources/data-source=*:add)&writable({selected.profile}/subsystem=datasources/data-source=*@enabled)";
        Constraints constraints = Constraints.parse(input);

        assertEquals(2, constraints.size());
        assertEquals(AND, constraints.getOperator());
        assertTrue(constraints.contains(Constraint.executable(template, ADD)));
        assertTrue(constraints.contains(Constraint.writable(template, ENABLED)));
    }

    @Test
    public void parseOr() throws Exception {
        String input = "executable({selected.profile}/subsystem=datasources/data-source=*:add)|writable({selected.profile}/subsystem=datasources/data-source=*@enabled)";
        Constraints constraints = Constraints.parse(input);

        assertEquals(2, constraints.size());
        assertEquals(OR, constraints.getOperator());
        assertTrue(constraints.contains(Constraint.executable(template, ADD)));
        assertTrue(constraints.contains(Constraint.writable(template, ENABLED)));
    }
}
