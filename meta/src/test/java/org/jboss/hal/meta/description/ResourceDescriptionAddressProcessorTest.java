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
package org.jboss.hal.meta.description;

import org.jboss.hal.dmr.ResourceAddress;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

@SuppressWarnings({"HardCodedStringLiteral", "DuplicateStringLiteralInspection"})
public class ResourceDescriptionAddressProcessorTest {

    private ResourceDescriptionAddressProcessor processor;

    @Before
    public void setUp() throws Exception {
        processor = new ResourceDescriptionAddressProcessor();
    }

    @Test
    public void nil() throws Exception {
        ResourceAddress result = processor.apply(null);
        assertEquals(new ResourceAddress(), result);
    }

    @Test
    public void empty() throws Exception {
        ResourceAddress input = new ResourceAddress();
        ResourceAddress expected = new ResourceAddress();

        ResourceAddress result = processor.apply(input);
        assertEquals(expected, result);
    }

    @Test
    public void simple() throws Exception {
        ResourceAddress input = new ResourceAddress()
                .add("foo", "bar");
        ResourceAddress expected = new ResourceAddress()
                .add("foo", "bar");

        ResourceAddress result = processor.apply(input);
        assertEquals(expected, result);
    }

    @Test
    public void host() throws Exception {
        ResourceAddress input = new ResourceAddress()
                .add("host", "master");
        ResourceAddress expected = new ResourceAddress()
                .add("host", "master");

        ResourceAddress result = processor.apply(input);
        assertEquals(expected, result);
    }

    @Test
    public void host2() throws Exception {
        ResourceAddress input = new ResourceAddress()
                .add("host", "master")
                .add("subsystem", "jmx");
        ResourceAddress expected = new ResourceAddress()
                .add("host", "*")
                .add("subsystem", "jmx");

        ResourceAddress result = processor.apply(input);
        assertEquals(expected, result);
    }

    @Test
    public void server() throws Exception {
        ResourceAddress input = new ResourceAddress()
                .add("host", "*")
                .add("server", "server-one");
        ResourceAddress expected = new ResourceAddress()
                .add("host", "*")
                .add("server", "*");

        ResourceAddress result = processor.apply(input);
        assertEquals(expected, result);
    }

    @Test
    public void server2() throws Exception {
        ResourceAddress input = new ResourceAddress()
                .add("host", "*")
                .add("server", "server-one")
                .add("subsystem", "datasources")
                .add("data-source", "*");
        ResourceAddress expected = new ResourceAddress()
                .add("host", "*")
                .add("server", "*")
                .add("subsystem", "datasources")
                .add("data-source", "*");

        ResourceAddress result = processor.apply(input);
        assertEquals(expected, result);
    }

    @Test
    public void serverConfig() throws Exception {
        ResourceAddress input = new ResourceAddress()
                .add("host", "*")
                .add("server-config", "server-one");
        ResourceAddress expected = new ResourceAddress()
                .add("host", "*")
                .add("server-config", "*");

        ResourceAddress result = processor.apply(input);
        assertEquals(expected, result);
    }

    @Test
    public void serverConfig2() throws Exception {
        ResourceAddress input = new ResourceAddress()
                .add("host", "*")
                .add("server-config", "server-one")
                .add("jvm", "*");
        ResourceAddress expected = new ResourceAddress()
                .add("host", "*")
                .add("server-config", "*")
                .add("jvm", "*");

        ResourceAddress result = processor.apply(input);
        assertEquals(expected, result);
    }

    @Test
    public void serverGroup() throws Exception {
        ResourceAddress input = new ResourceAddress()
                .add("server-group", "main-server-group");
        ResourceAddress expected = new ResourceAddress()
                .add("server-group", "*");

        ResourceAddress result = processor.apply(input);
        assertEquals(expected, result);
    }

    @Test
    public void serverGroup2() throws Exception {
        ResourceAddress input = new ResourceAddress()
                .add("server-group", "main-server-group")
                .add("system-property", "*");
        ResourceAddress expected = new ResourceAddress()
                .add("server-group", "*")
                .add("system-property", "*");

        ResourceAddress result = processor.apply(input);
        assertEquals(expected, result);
    }

    @Test
    public void profile() throws Exception {
        ResourceAddress input = new ResourceAddress()
                .add("profile", "full");
        ResourceAddress expected = new ResourceAddress()
                .add("profile", "*");

        ResourceAddress result = processor.apply(input);
        assertEquals(expected, result);
    }

    @Test
    public void profile2() throws Exception {
        ResourceAddress input = new ResourceAddress()
                .add("profile", "full")
                .add("subsystem", "mail");
        ResourceAddress expected = new ResourceAddress()
                .add("profile", "*")
                .add("subsystem", "mail");

        ResourceAddress result = processor.apply(input);
        assertEquals(expected, result);
    }

    @Test
    public void hostInTheMiddle() throws Exception {
        ResourceAddress input = new ResourceAddress()
                .add("profile", "full")
                .add("subsystem", "undertow")
                .add("server", "*")
                .add("host", "*")
                .add("location", "*");
        ResourceAddress expected = new ResourceAddress()
                .add("profile", "*")
                .add("subsystem", "undertow")
                .add("server", "*")
                .add("host", "*")
                .add("location", "*");

        ResourceAddress result = processor.apply(input);
        assertEquals(expected, result);
    }

    @Test
    public void serverAtTheEnd() throws Exception {
        ResourceAddress input = new ResourceAddress()
                .add("profile", "full")
                .add("subsystem", "mail")
                .add("mail-session", "default")
                .add("server", "imap");
        ResourceAddress expected = new ResourceAddress()
                .add("profile", "*")
                .add("subsystem", "mail")
                .add("mail-session", "default")
                .add("server", "imap");

        ResourceAddress result = processor.apply(input);
        assertEquals(expected, result);
    }
}