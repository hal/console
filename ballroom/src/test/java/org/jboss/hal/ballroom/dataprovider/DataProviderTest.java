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
package org.jboss.hal.ballroom.dataprovider;

import java.util.Arrays;
import java.util.function.Function;

import com.google.common.primitives.Ints;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatcher;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.primitives.Ints.asList;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;

@SuppressWarnings({"HardCodedStringLiteral", "DuplicateStringLiteralInspection", "unchecked"})
public class DataProviderTest {

    private static class ItemsMatcher implements ArgumentMatcher<Iterable<Integer>> {

        private final int[] expected;

        ItemsMatcher(int[] expected) {this.expected = expected;}

        @Override
        public String toString() {
            return Arrays.toString(expected);
        }

        @Override
        public boolean matches(Iterable<Integer> argument) {
            return Arrays.equals(expected, toArray(argument));
        }
    }

    private static Iterable<Integer> itemsMatcher(int[] items) {
        return argThat(new ItemsMatcher(items));
    }

    private static int[] toArray(Iterable<Integer> iterable) {
        return Ints.toArray(newArrayList(iterable));
    }


    private static final int PAGE_SIZE = 10;
    private static final Function<Integer, String> IDENTIFIER = String::valueOf;

    private DataProvider<Integer> dataProvider;
    private Display<Integer> display;

    @Before
    public void setUp() throws Exception {
        dataProvider = new DataProvider<>(IDENTIFIER, false, PAGE_SIZE);
        display = mock(Display.class);
        dataProvider.addDisplay(display);
    }


    // ------------------------------------------------------ items

    @Test
    public void updateItems() throws Exception {
        int[] items = items(PAGE_SIZE);
        dataProvider.update(asList(items));
        assertAllFilteredVisible(items, items, items);
    }

    @Test
    public void contains() throws Exception {
        int[] items = items(23);
        dataProvider.update(asList(items));
        assertTrue(dataProvider.contains(0));
        assertTrue(dataProvider.contains(9));
        assertTrue(dataProvider.contains(10));
        assertTrue(dataProvider.contains(22));
        assertFalse(dataProvider.contains(23));
    }

    @Test
    public void visible() throws Exception {
        int[] items = items(23);
        dataProvider.update(asList(items));
        assertTrue(dataProvider.isVisible(0));
        assertTrue(dataProvider.isVisible(9));
        assertFalse(dataProvider.isVisible(10));
        assertFalse(dataProvider.isVisible(22));
        assertFalse(dataProvider.isVisible(23));
    }


    // ------------------------------------------------------ page size

    @Test
    public void setPageSize() throws Exception {
        int[] items = items(PAGE_SIZE);
        dataProvider.update(asList(items));
        verify(display).showItems(itemsMatcher(items(PAGE_SIZE)), eq(new PageInfo(0, PAGE_SIZE, PAGE_SIZE, PAGE_SIZE)));
        assertAllFilteredVisible(items, items, items(PAGE_SIZE));

        dataProvider.setPageSize(6);
        dataProvider.update();
        verify(display).showItems(itemsMatcher(items(6)), eq(new PageInfo(0, 6, 6, PAGE_SIZE)));
        assertAllFilteredVisible(items, items, items(6));
    }

    @Test
    public void getPageSize() throws Exception {
        dataProvider.update(asList(items(PAGE_SIZE)));
        assertEquals(1, dataProvider.getPages());

        dataProvider.update(asList(items(PAGE_SIZE - 1)));
        assertEquals(1, dataProvider.getPages());

        dataProvider.update(asList(items(PAGE_SIZE + 1)));
        assertEquals(2, dataProvider.getPages());
    }

    @Test
    public void illegalPageSize() throws Exception {
        dataProvider.setPageSize(Integer.MIN_VALUE);
        assertEquals(1, dataProvider.getPageSize());
    }


    // ------------------------------------------------------ paging

    @Test
    public void firstPage() throws Exception {
        int[] items = items(42);
        dataProvider.update(asList(items));

        reset(display);
        dataProvider.setPage(0);
        dataProvider.update();
        verify(display).showItems(itemsMatcher(items(PAGE_SIZE)), eq(new PageInfo(0, PAGE_SIZE, PAGE_SIZE, 42)));
        assertAllFilteredVisible(items, items, items(PAGE_SIZE));
    }

    @Test
    public void midPage() throws Exception {
        int[] items = items(42);
        dataProvider.update(asList(items));

        reset(display);
        dataProvider.setPage(2);
        dataProvider.update();
        verify(display).showItems(itemsMatcher(items(20, 29)), eq(new PageInfo(2, PAGE_SIZE, 10, 42)));
        assertAllFilteredVisible(items, items, items(20, 29));
    }

    @Test
    public void lastPage() throws Exception {
        int[] items = items(42);
        dataProvider.update(asList(items));

        reset(display);
        dataProvider.setPage(4);
        dataProvider.update();
        verify(display).showItems(itemsMatcher(items(40, 41)), eq(new PageInfo(4, PAGE_SIZE, 2, 42)));
        assertAllFilteredVisible(items, items, items(40, 41));
    }

    @Test
    public void illegalPage() throws Exception {
        dataProvider.update(asList(items(2 * PAGE_SIZE)));
        dataProvider.setPage(Integer.MIN_VALUE);
        assertEquals(0, dataProvider.getPage());
        dataProvider.setPage(Integer.MAX_VALUE);
        assertEquals(1, dataProvider.getPage());

        dataProvider.update(asList(items(2 * PAGE_SIZE + 1)));
        dataProvider.setPage(Integer.MAX_VALUE);
        assertEquals(2, dataProvider.getPage());
    }


    // ------------------------------------------------------ helper methods

    private void assertAllFilteredVisible(int[] all, int[] filtered, int[] visible) {
        assertArrayEquals(all, toArray(dataProvider.getAllItems()));
        assertArrayEquals(filtered, toArray(dataProvider.getFilteredItems()));
        assertArrayEquals(visible, toArray(dataProvider.getVisibleItems()));
    }

    private void assertNoSelection() {
        assertFalse(dataProvider.hasSelection());
        assertNull(dataProvider.getSingleSelection());
        assertTrue(dataProvider.getSelection().isEmpty());
    }

    private int[] items(int size) {
        return items(0, size - 1);
    }

    // to is *inclusive*
    private int[] items(int from, int to) {
        int size = to - from + 1;
        int[] items = new int[size];
        for (int i = 0; i < size; i++) {
            items[i] = from + i;
        }
        return items;
    }
}