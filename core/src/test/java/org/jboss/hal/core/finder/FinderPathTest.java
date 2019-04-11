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
package org.jboss.hal.core.finder;

import java.util.Iterator;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@SuppressWarnings("HardCodedStringLiteral")
public class FinderPathTest {

    @Test
    public void fromNull() {
        assertTrue(FinderPath.from(null).isEmpty());
    }

    @Test
    public void fromEmpty() {
        assertTrue(FinderPath.from("").isEmpty());
    }

    @Test
    public void fromBlank() {
        assertTrue(FinderPath.from("     ").isEmpty());
    }

    @Test(expected = IllegalArgumentException.class)
    public void fromIllegal() {
        assertTrue(FinderPath.from("foo").isEmpty());
    }

    @Test
    public void appendIds() {
        FinderPath path = new FinderPath().append("foo", "bar");
        assertFalse(path.isEmpty());
        assertEquals(1, path.size());
        assertSegment(path.iterator().next(), "foo", "bar");
    }

    @Test
    public void appendIdsAndTitles() {
        FinderPath path = new FinderPath().append("foo", "bar", "Foo", "Bar");
        assertFalse(path.isEmpty());
        assertEquals(1, path.size());
        assertSegment(path.iterator().next(), "foo", "bar", "Foo", "Bar");
    }

    @Test
    public void clear() {
        FinderPath path = new FinderPath().append("foo", "bar");
        assertFalse(path.isEmpty());
        assertEquals(1, path.size());

        path.clear();
        assertTrue(path.isEmpty());
        assertEquals(0, path.size());
    }

    @Test
    public void reversed() {
        FinderPath path = new FinderPath().append("1", "one").append("2", "two").reversed();
        assertFalse(path.isEmpty());
        assertEquals(2, path.size());

        Iterator<FinderSegment> iterator = path.iterator();
        assertSegment(iterator.next(), "2", "two");
        assertSegment(iterator.next(), "1", "one");
    }

    @Test
    public void asString() {
        assertEquals(
                "1" + FinderSegment.SEPARATOR + "one" + FinderPath.SEPARATOR + "2" + FinderSegment.SEPARATOR + "two",
                new FinderPath().append("1", "one").append("2", "two").toString());
    }

    private void assertSegment(FinderSegment segment, String columnId, String itemId) {
        assertSegment(segment, columnId, itemId, columnId, itemId);
    }

    private void assertSegment(FinderSegment segment,
            String columnId, String itemId, String columnTitle, String itemTitle) {
        assertEquals(columnId, segment.getColumnId());
        assertEquals(itemId, segment.getItemId());
        assertEquals(columnTitle, segment.getColumnTitle());
        assertEquals(itemTitle, segment.getItemTitle());
    }
}