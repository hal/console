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
package org.jboss.hal.meta.description;

import org.jboss.hal.meta.AddressTemplate;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Harald Pehl
 */
@SuppressWarnings("DuplicateStringLiteralInspection")
public class ResourceDescriptionTemplateProcessorTest {

    private ResourceDescriptionTemplateProcessor processor;

    @Before
    public void setUp() throws Exception {
        processor = new ResourceDescriptionTemplateProcessor();
    }

    @Test
    public void nil() throws Exception {
        AddressTemplate result = processor.apply(null);
        assertEquals(AddressTemplate.ROOT, result);
    }

    @Test
    public void empty() throws Exception {
        AddressTemplate input = AddressTemplate.of("");
        AddressTemplate result = processor.apply(input);
        assertEquals(AddressTemplate.ROOT, result);
    }

    @Test
    public void simple() throws Exception {
        AddressTemplate input = AddressTemplate.of("/foo=bar");
        AddressTemplate expected = AddressTemplate.of("/foo=bar");

        AddressTemplate result = processor.apply(input);
        assertEquals(expected, result);
    }

    @Test
    public void host() throws Exception {
        AddressTemplate input = AddressTemplate.of("/host=master");
        AddressTemplate expected = AddressTemplate.of("/host=*");

        AddressTemplate result = processor.apply(input);
        assertEquals(expected, result);
    }

    @Test
    public void host2() throws Exception {
        AddressTemplate input = AddressTemplate.of("/host=master/subsystem=jmx");
        AddressTemplate expected = AddressTemplate.of("/host=*/subsystem=jmx");

        AddressTemplate result = processor.apply(input);
        assertEquals(expected, result);
    }

    @Test
    public void server() throws Exception {
        AddressTemplate input = AddressTemplate.of("/host=master/server=server-one");
        AddressTemplate expected = AddressTemplate.of("/host=*/server=*");

        AddressTemplate result = processor.apply(input);
        assertEquals(expected, result);
    }

    @Test
    public void server2() throws Exception {
        AddressTemplate input = AddressTemplate.of(
                "/host=master/server=server-one/subsystem=datasources/data-source=*");
        AddressTemplate expected = AddressTemplate.of("/host=*/server=*/subsystem=datasources/data-source=*");

        AddressTemplate result = processor.apply(input);
        assertEquals(expected, result);
    }

    @Test
    public void serverConfig() throws Exception {
        AddressTemplate input = AddressTemplate.of("/host=master/server-config=server-one");
        AddressTemplate expected = AddressTemplate.of("/host=*/server-config=*");

        AddressTemplate result = processor.apply(input);
        assertEquals(expected, result);
    }

    @Test
    public void serverConfig2() throws Exception {
        AddressTemplate input = AddressTemplate.of("/host=master/server-config=server-one/jvm=*");
        AddressTemplate expected = AddressTemplate.of("/host=*/server-config=*/jvm=*");

        AddressTemplate result = processor.apply(input);
        assertEquals(expected, result);
    }

    @Test
    public void serverGroup() throws Exception {
        AddressTemplate input = AddressTemplate.of("/server-group=main-server-group");
        AddressTemplate expected = AddressTemplate.of("/server-group=*");

        AddressTemplate result = processor.apply(input);
        assertEquals(expected, result);
    }

    @Test
    public void serverGroup2() throws Exception {
        AddressTemplate input = AddressTemplate.of("/server-group=main-server-group/system-property=*");
        AddressTemplate expected = AddressTemplate.of("/server-group=*/system-property=*");

        AddressTemplate result = processor.apply(input);
        assertEquals(expected, result);
    }

    @Test
    public void profile() throws Exception {
        AddressTemplate input = AddressTemplate.of("/profile=full");
        AddressTemplate expected = AddressTemplate.of("/profile=*");

        AddressTemplate result = processor.apply(input);
        assertEquals(expected, result);
    }

    @Test
    public void profile2() throws Exception {
        AddressTemplate input = AddressTemplate.of("/profile=full/subsystem=mail");
        AddressTemplate expected = AddressTemplate.of("/profile=*/subsystem=mail");

        AddressTemplate result = processor.apply(input);
        assertEquals(expected, result);
    }

    @Test
    public void hostInTheMiddle() throws Exception {
        AddressTemplate input = AddressTemplate.of("/profile=full/subsystem=undertow/server=*/host=*/location=*");
        AddressTemplate expected = AddressTemplate.of("/profile=*/subsystem=undertow/server=*/host=*/location=*");

        AddressTemplate result = processor.apply(input);
        assertEquals(expected, result);
    }

    @Test
    public void serverAtTheEnd() throws Exception {
        AddressTemplate input = AddressTemplate.of("/profile=full/subsystem=mail/mail-session=default/server=imap");
        AddressTemplate expected = AddressTemplate.of("/profile=*/subsystem=mail/mail-session=default/server=imap");

        AddressTemplate result = processor.apply(input);
        assertEquals(expected, result);
    }
}