/*
 *  Copyright 2022 Red Hat
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.jboss.hal.ballroom;

import org.junit.Before;
import org.junit.Test;

import com.google.gwt.junit.GWTMockUtilities;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;

@SuppressWarnings({ "HardCodedStringLiteral", "DuplicateStringLiteralInspection" })
public class LabelBuilderTest {

    private LabelBuilder builder;

    @Before
    public void setUp() {
        GWTMockUtilities.disarm();
        builder = new LabelBuilder();
    }

    @Test
    public void capitalize() {
        assertEquals("Background Validation", builder.label("background-validation"));
        assertEquals("Enabled", builder.label("enabled"));
    }

    @Test
    public void specials() {
        assertEquals("Check Valid Connection SQL", builder.label("check-valid-connection-sql"));
        assertEquals("Connection URL", builder.label("connection-url"));
        assertEquals("JNDI Name", builder.label("jndi-name"));
        assertEquals("URL Selector Strategy Class Name", builder.label("url-selector-strategy-class-name"));
        assertEquals("Modify WSDL Address", builder.label("modify-wsdl-address"));
        assertEquals("WSDL Port", builder.label("wsdl-port"));
    }

    @Test
    public void enumeration() throws Exception {
        assertEquals("'First'", builder.enumeration(singletonList("first"), "and"));
        assertEquals("'First' or 'Second'", builder.enumeration(asList("first", "second"), "or"));
        assertEquals("'First', 'Second' and / or 'Third'",
                builder.enumeration(asList("first", "second", "third"), "and / or"));
    }

    @Test
    public void label() throws Exception {
        assertEquals("Profile Name", builder.label("Profile Name"));

    }
}
