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
import static org.junit.Assert.assertNull;

@SuppressWarnings({"HardCodedStringLiteral", "DuplicateStringLiteralInspection", "SpellCheckingInspection"})
public class StringsTest {

    @Test
    public void abbreviateMiddle() throws Exception {
        assertNull(Strings.abbreviateMiddle(null, 0));
        assertNull(Strings.abbreviateMiddle(null, 1));
        assertNull(Strings.abbreviateMiddle(null, 100));

        assertEquals("", Strings.abbreviateMiddle("", 0));
        assertEquals("", Strings.abbreviateMiddle("", 1));
        assertEquals("", Strings.abbreviateMiddle("", 100));

        assertEquals("", Strings.abbreviateMiddle("foo", 0));
        assertEquals("f", Strings.abbreviateMiddle("foo", 1));
        assertEquals("fo", Strings.abbreviateMiddle("foo", 2));
        assertEquals("foo", Strings.abbreviateMiddle("foo", 3));
        assertEquals("foo", Strings.abbreviateMiddle("foo", 4));
        assertEquals("foo", Strings.abbreviateMiddle("foo", 100));

        assertEquals("", Strings.abbreviateMiddle("foobar", 0));
        assertEquals("f", Strings.abbreviateMiddle("foobar", 1));
        assertEquals("fo", Strings.abbreviateMiddle("foobar", 2));
        assertEquals("foo", Strings.abbreviateMiddle("foobar", 3));
        assertEquals("f...", Strings.abbreviateMiddle("foobar", 4));
        assertEquals("f...r", Strings.abbreviateMiddle("foobar", 5));
        assertEquals("foobar", Strings.abbreviateMiddle("foobar", 6));
        assertEquals("foobar", Strings.abbreviateMiddle("foobar", 7));
        assertEquals("foobar", Strings.abbreviateMiddle("foobar", 100));

        assertEquals("", Strings.abbreviateMiddle("Lorem ipsum dolor sit amet", 0));
        assertEquals("L", Strings.abbreviateMiddle("Lorem ipsum dolor sit amet", 1));
        assertEquals("Lore...met", Strings.abbreviateMiddle("Lorem ipsum dolor sit amet", 10));
        assertEquals("Lorem ipsum dolor sit amet", Strings.abbreviateMiddle("Lorem ipsum dolor sit amet", 26));
        assertEquals("Lorem ipsum dolor sit amet", Strings.abbreviateMiddle("Lorem ipsum dolor sit amet", 100));
    }

    @Test
    public void abbreviateFqClassname() throws Exception {
        assertEquals("c.a.main", Strings.abbreviateFqClassName("com.acme.main"));
        assertEquals("c.a.main.Main", Strings.abbreviateFqClassName("com.acme.main.Main"));
        assertEquals("c.a.l.p.foo.Bar", Strings.abbreviateFqClassName("com.acme.longer.package.foo.Bar"));
    }

    @Test
    public void substringAfterLast() throws Exception {
        assertNull(Strings.substringAfterLast(null, "/"));
        assertEquals("", Strings.substringAfterLast("", "/"));
        assertEquals("", Strings.substringAfterLast("a", "/"));
        assertEquals("c", Strings.substringAfterLast("a/b/c", "/"));
    }

    @Test
    public void getParent() throws Exception {
        assertNull(Strings.getParent(null));
        assertEquals("", Strings.getParent(""));
        assertNull(Strings.getParent("a"));
        assertEquals("/", Strings.getParent("/"));
        assertEquals("/", Strings.getParent("/a"));
        assertEquals("/a", Strings.getParent("/a/b"));
        assertEquals("/a/b", Strings.getParent("/a/b/c"));
        assertEquals("/a", Strings.getParent(Strings.getParent("/a/b/c")));
        assertEquals("a", Strings.getParent("a/b"));
        assertEquals("a/b", Strings.getParent("a/b/c"));
        assertEquals("a", Strings.getParent(Strings.getParent("a/b/c")));
    }

    @Test
    public void getDomain() throws Exception {
        assertNull(Strings.getDomain(null));
        assertEquals("", Strings.getDomain(""));
        assertEquals("a", Strings.getDomain("a"));
        assertEquals("http://acme.com", Strings.getDomain("http://acme.com"));
        assertEquals("http://acme.com", Strings.getDomain("http://acme.com/"));
        assertEquals("http://acme.com", Strings.getDomain("http://acme.com/foo"));
        assertEquals("http://acme.com:1234", Strings.getDomain("http://acme.com:1234?"));
        assertEquals("http://acme.com:1234", Strings.getDomain("http://acme.com:1234?foo=bar/index.html#id"));
    }
}