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
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ConstraintTest {

    private static final AddressTemplate TEMPLATE = AddressTemplate.of("j=o/l=o/p=o");
    private static final String OPERATION = "engage";
    private static final String ATTRIBUTE = "NCC-1701-D";
    private static final String ENGAGE_DATA = "executable(j=o/l=o/p=o:engage)";
    private static final String NCC_DATA = "writable(j=o/l=o/p=o@NCC-1701-D)";

    private Constraint ex, wr;

    @Before
    public void setUp() {
        ex = Constraint.executable(TEMPLATE, OPERATION);
        wr = Constraint.writable(TEMPLATE, ATTRIBUTE);
    }

    @Test
    public void name() {
        assertEquals(OPERATION, ex.getName());
        assertEquals(ATTRIBUTE, wr.getName());
    }

    @Test
    public void permission() {
        assertEquals(Permission.EXECUTABLE, ex.getPermission());
        assertEquals(Permission.WRITABLE, wr.getPermission());
    }

    @Test
    public void target() {
        assertEquals(Target.OPERATION, ex.getTarget());
        assertEquals(Target.ATTRIBUTE, wr.getTarget());
    }

    @Test
    public void tamplate() {
        assertEquals(TEMPLATE, ex.getTemplate());
        assertEquals(TEMPLATE, wr.getTemplate());
    }

    @Test
    public void data() {
        assertEquals(ENGAGE_DATA, ex.data());
        assertEquals(ENGAGE_DATA, ex.toString());
        assertEquals(NCC_DATA, wr.data());
        assertEquals(NCC_DATA, wr.toString());
    }

    @Test
    public void parse() {
        assertEquals(ex, Constraint.parse(ENGAGE_DATA));
        assertEquals(wr, Constraint.parse(NCC_DATA));
    }

    @Test(expected = IllegalArgumentException.class)
    public void parseNull() {
        Constraint.parse(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void parseEmpty() {
        Constraint.parse("");
    }

    @Test(expected = IllegalArgumentException.class)
    public void parseBlank() {
        Constraint.parse("   ");
    }

    @Test(expected = IllegalArgumentException.class)
    public void parseIllegal() {
        Constraint.parse("not a constraint");
    }
}