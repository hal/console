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
package org.jboss.hal.resources;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

@SuppressWarnings("DuplicateStringLiteralInspection")
public class IdsTest {

    @Test
    public void simple() {
        assertEquals("foo", Ids.build("foo"));
    }

    @Test
    public void complex() {
        assertEquals("foo-bar-1-2-3", Ids.build("foo", "bar", "1-2", "3"));
    }

    @Test
    public void emptyNull() {
        assertEquals("foo-bar", Ids.build("foo", "", null, "", "", "bar", ""));
    }

    @Test(expected = IllegalArgumentException.class)
    public void illegal() {
        Ids.build(null);
    }

    @Test
    public void asId() {
        assertEquals("lorem-ipsum", Ids.asId("lorem-ipsum"));
        assertEquals("lorem-ipsum", Ids.asId("Lorem Ipsum"));
        assertEquals("l0rem-ip5um", Ids.asId("l0rem-ip5um"));
        assertEquals("lorem--ipsum", Ids.asId("lorem §±!@#$%^&*()=_+[]{};'\\:\"|,./<>?`~ ipsum"));
    }

    @Test
    public void build() {
        assertEquals("lorem-ipsum", Ids.build("lorem-ipsum"));
        assertEquals("lorem-ipsum", Ids.build("Lorem Ipsum"));
        assertEquals("lorem-ipsum", Ids.build("Lorem", "Ipsum"));
        assertEquals("lorem-ipsum", Ids.build(" Lorem ", " Ipsum "));
        assertEquals("l0rem-ip5um", Ids.build("l0rem ip5um"));
        assertEquals("l0rem-ip5um", Ids.build("l0rem", "ip5um"));
        assertEquals("l0rem-ip5um", Ids.build(" l0rem ", " ip5um "));
        assertEquals("lorem--ipsum", Ids.build("lorem §±!@#$%^&*()=_+[]{};'\\:\"|,./<>?`~ ipsum"));
        assertEquals("lorem--ipsum", Ids.build("lorem", "§±!@#$%^&*()=_+[]{};'\\:\"|,./<>?`~", "ipsum"));
    }
}