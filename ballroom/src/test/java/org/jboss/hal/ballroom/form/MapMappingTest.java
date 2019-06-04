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
package org.jboss.hal.ballroom.form;

import org.junit.Before;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.*;

public class MapMappingTest {

    private static final String KEY = "key";
    private static final String VALUE = "VALUE";
    private static final String KEY_VALUE = KEY + "=" + VALUE;
    private static final String SPECIAL_VALUE = "value-with-special-characters-like.-:-@-;-=-?-!-#-$-%-&-[-]";
    private static final String KEY_SPECIAL_VALUE = KEY + "=" + SPECIAL_VALUE;

    private PropertiesItem.MapMapping mapping;

    @Before
    public void setUp() {
        mapping = new PropertiesItem.MapMapping();
    }

    @Test
    public void parseSimple() {
        Map<String, String> map = mapping.parseTag(KEY_VALUE);
        assertNotNull(map);
        assertEquals(1, map.size());
        assertEquals(VALUE, map.get(KEY));
    }

    @Test
    public void parseSpecial() {
        Map<String, String> map = mapping.parseTag(KEY_SPECIAL_VALUE);
        assertNotNull(map);
        assertEquals(1, map.size());
        assertEquals(SPECIAL_VALUE, map.get(KEY));
    }

    @Test
    public void validateNull() {
        assertFalse(mapping.validator().validate(null));
    }

    @Test
    public void validateEmpty() {
        assertFalse(mapping.validator().validate(""));
    }

    @Test
    public void validateBlank() {
        assertFalse(mapping.validator().validate("    "));
    }

    @Test
    public void validateSimple() {
        assertTrue(mapping.validator().validate(KEY_VALUE));
    }

    @Test
    public void validateSpecial() {
        assertTrue(mapping.validator().validate(KEY_SPECIAL_VALUE));
    }
}