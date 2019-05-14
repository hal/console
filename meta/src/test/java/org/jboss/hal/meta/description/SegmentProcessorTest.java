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

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

@SuppressWarnings({"HardCodedStringLiteral", "DuplicateStringLiteralInspection"})
public class SegmentProcessorTest {

    static class AsListConsumer implements Consumer<String[]> {

        final List<String[]> result;

        AsListConsumer() {result = new ArrayList<>();}

        @Override
        public void accept(String[] strings) {
            result.add(strings);
        }
    }


    static class Fixture {

        static class Builder {

            final String message;
            final List<String[]> segments;
            final List<String[]> expected;

            Builder(String message) {
                this.message = message;
                this.segments = new ArrayList<>();
                this.expected = new ArrayList<>();
            }

            Builder segments(String... segments) {
                if (segments != null) {
                    for (int i = 0; i < segments.length; i++) {
                        this.segments.add(new String[]{segments[i], segments[i + 1]});
                        i++;
                    }
                }
                return this;
            }

            Builder expected(String... expected) {
                if (expected != null) {
                    for (int i = 0; i < expected.length; i++) {
                        this.expected.add(new String[]{expected[i], expected[i + 1]});
                        i++;
                    }
                }
                return this;
            }

            Fixture build() {
                return new Fixture(this);
            }
        }


        final String message;
        final List<String[]> segments;
        final List<String[]> expected;

        Fixture(Builder builder) {
            this.message = builder.message;
            this.segments = builder.segments;
            this.expected = builder.expected;
        }
    }


    private AsListConsumer consumer;

    @Before
    public void setUp() throws Exception {
        consumer = new AsListConsumer();
    }

    @Test
    public void nil() throws Exception {
        Fixture fixture = new Fixture.Builder("null")
                .segments()
                .expected()
                .build();
        SegmentProcessor.process(fixture.segments, consumer);
        assertFixture(fixture, consumer.result);
    }

    @Test
    public void empty() throws Exception {
        Fixture fixture = new Fixture.Builder("empty")
                .segments("", "")
                .expected("", "")
                .build();
        SegmentProcessor.process(fixture.segments, consumer);
        assertFixture(fixture, consumer.result);
    }

    @Test
    public void simple() throws Exception {
        Fixture fixture = new Fixture.Builder("simple")
                .segments("foo", "bar")
                .expected("foo", "bar")
                .build();
        SegmentProcessor.process(fixture.segments, consumer);
        assertFixture(fixture, consumer.result);
    }

    @Test
    public void host() throws Exception {
        Fixture fixture = new Fixture.Builder("host")
                .segments("host", "master")
                .expected("host", "master")
                .build();
        SegmentProcessor.process(fixture.segments, consumer);
        assertFixture(fixture, consumer.result);
    }

    @Test
    public void host2() throws Exception {
        Fixture fixture = new Fixture.Builder("host2")
                .segments("host", "master", "subsystem", "jmx")
                .expected("host", "*", "subsystem", "jmx")
                .build();
        SegmentProcessor.process(fixture.segments, consumer);
        assertFixture(fixture, consumer.result);
    }

    @Test
    public void server() throws Exception {
        Fixture fixture = new Fixture.Builder("server")
                .segments("host", "*", "server", "server-one")
                .expected("host", "*", "server", "*")
                .build();
        SegmentProcessor.process(fixture.segments, consumer);
        assertFixture(fixture, consumer.result);
    }

    @Test
    public void server2() throws Exception {
        Fixture fixture = new Fixture.Builder("server2")
                .segments("host", "*", "server", "server-one", "subsystem", "datasources", "data-source", "*")
                .expected("host", "*", "server", "*", "subsystem", "datasources", "data-source", "*")
                .build();
        SegmentProcessor.process(fixture.segments, consumer);
        assertFixture(fixture, consumer.result);
    }

    @Test
    public void serverConfig() throws Exception {
        Fixture fixture = new Fixture.Builder("server-config")
                .segments("host", "*", "server-config", "server-one")
                .expected("host", "*", "server-config", "*")
                .build();
        SegmentProcessor.process(fixture.segments, consumer);
        assertFixture(fixture, consumer.result);
    }

    @Test
    public void serverConfig2() throws Exception {
        Fixture fixture = new Fixture.Builder("server-config2")
                .segments("host", "*", "server-config", "server-one", "jvm", "*")
                .expected("host", "*", "server-config", "*", "jvm", "*")
                .build();
        SegmentProcessor.process(fixture.segments, consumer);
        assertFixture(fixture, consumer.result);
    }

    @Test
    public void serverGroup() throws Exception {
        Fixture fixture = new Fixture.Builder("server-group")
                .segments("server-group", "main-server-group")
                .expected("server-group", "*")
                .build();
        SegmentProcessor.process(fixture.segments, consumer);
        assertFixture(fixture, consumer.result);
    }

    @Test
    public void serverGroup2() throws Exception {
        Fixture fixture = new Fixture.Builder("server-group2")
                .segments("server-group", "main-server-group", "system-property", "*")
                .expected("server-group", "*", "system-property", "*")
                .build();
        SegmentProcessor.process(fixture.segments, consumer);
        assertFixture(fixture, consumer.result);
    }

    @Test
    public void profile() throws Exception {
        Fixture fixture = new Fixture.Builder("profile")
                .segments("profile", "full")
                .expected("profile", "*")
                .build();
        SegmentProcessor.process(fixture.segments, consumer);
        assertFixture(fixture, consumer.result);
    }

    @Test
    public void profile2() throws Exception {
        Fixture fixture = new Fixture.Builder("profile2")
                .segments("profile", "full", "subsystem", "mail")
                .expected("profile", "*", "subsystem", "mail")
                .build();
        SegmentProcessor.process(fixture.segments, consumer);
        assertFixture(fixture, consumer.result);
    }

    @Test
    public void hostInTheMiddle() throws Exception {
        Fixture fixture = new Fixture.Builder("host-in-the-middle")
                .segments("profile", "full", "subsystem", "undertow", "server", "*", "host", "*", "location", "*")
                .expected("profile", "*", "subsystem", "undertow", "server", "*", "host", "*", "location", "*")
                .build();
        SegmentProcessor.process(fixture.segments, consumer);
        assertFixture(fixture, consumer.result);
    }

    @Test
    public void serverAtTheEnd() throws Exception {
        Fixture fixture = new Fixture.Builder("server-at-the-end")
                .segments("profile", "full", "subsystem", "mail", "mail-session", "default", "server", "imap")
                .expected("profile", "*", "subsystem", "mail", "mail-session", "default", "server", "imap")
                .build();
        SegmentProcessor.process(fixture.segments, consumer);
        assertFixture(fixture, consumer.result);
    }

    private void assertFixture(Fixture fixture, List<String[]> actuals) {
        assertEquals("Size: " + fixture.message, fixture.expected.size(), actuals.size());
        for (int i = 0; i < fixture.expected.size(); i++) {
            String[] expected = fixture.expected.get(i);
            String[] actual = actuals.get(i);
            assertArrayEquals("Index " + i + ": ", expected, actual);
        }
    }
}