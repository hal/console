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
package org.jboss.hal.meta.security;

import org.jboss.hal.meta.AddressTemplate;
import org.junit.Test;

import static org.jboss.hal.meta.security.Constraints.Operator.AND;
import static org.jboss.hal.meta.security.Constraints.Operator.OR;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ConstraintsTest {

    private static final Constraint ENGAGE = Constraint.executable(AddressTemplate.of("j/l/p"), "engage");
    private static final Constraint NCC = Constraint.writable(AddressTemplate.of("da/ta"), "NCC-1701-D");
    private static final String ENGAGE_DATA = "executable(j/l/p:engage)";
    private static final String NCC_DATA = "writable(da/ta@NCC-1701-D)";
    private static final String AND_DATA = ENGAGE_DATA + "&" + NCC_DATA;
    private static final String OR_DATA = ENGAGE_DATA + "|" + NCC_DATA;

    @Test
    public void empty() {
        Constraints empty = Constraints.empty();
        assertEmpty(empty);
    }

    @Test
    public void single() {
        Constraints single = Constraints.single(ENGAGE);
        assertSingle(single);
    }

    @Test
    public void and() {
        Constraints and = Constraints.and(ENGAGE, NCC);
        assertAnd(and);
    }

    @Test
    public void or() {
        Constraints or = Constraints.or(ENGAGE, NCC);
        assertOr(or);
    }

    @Test
    public void parseSingle() {
        Constraints single = Constraints.parse(ENGAGE_DATA);
        assertSingle(single);
    }

    @Test
    public void parseAnd() {
        Constraints and = Constraints.parse(AND_DATA);
        assertAnd(and);
    }

    @Test
    public void parseOr() {
        Constraints or = Constraints.parse(OR_DATA);
        assertOr(or);
    }

    @Test
    public void parseNull() {
        Constraints constraints = Constraints.parse(null);
        assertEmpty(constraints);
    }

    @Test
    public void parseEmpty() {
        Constraints constraints = Constraints.parse("");
        assertEmpty(constraints);
    }

    @Test
    public void parseBlank() {
        Constraints constraints = Constraints.parse("   ");
        assertEmpty(constraints);
    }

    @Test
    public void parseIllegal() {
        Constraints constraints = Constraints.parse("no constraints");
        assertEmpty(constraints);
    }


    // ------------------------------------------------------ helper methods

    private void assertEmpty(Constraints empty) {
        assertTrue(empty.isEmpty());
        assertTrue(empty.getConstraints().isEmpty());
        assertEquals(AND, empty.getOperator());
        assertEquals(0, empty.size());
        assertEquals("", empty.data());
        assertEquals("", empty.toString());
    }

    private void assertSingle(Constraints single) {
        assertFalse(single.isEmpty());
        assertFalse(single.getConstraints().isEmpty());
        assertEquals(AND, single.getOperator());
        assertEquals(1, single.size());
        assertEquals(ENGAGE.data(), single.data());
        assertEquals(ENGAGE.toString(), single.toString());
    }

    private void assertAnd(Constraints and) {
        assertFalse(and.isEmpty());
        assertFalse(and.getConstraints().isEmpty());
        assertEquals(AND, and.getOperator());
        assertEquals(2, and.size());
        assertEquals(AND_DATA, and.data());
        assertEquals(AND_DATA, and.toString());
    }

    private void assertOr(Constraints or) {
        assertFalse(or.isEmpty());
        assertFalse(or.getConstraints().isEmpty());
        assertEquals(OR, or.getOperator());
        assertEquals(2, or.size());
        assertEquals(OR_DATA, or.data());
        assertEquals(OR_DATA, or.toString());
    }
}
