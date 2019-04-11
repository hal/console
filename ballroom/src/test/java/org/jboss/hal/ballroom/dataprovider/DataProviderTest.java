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
package org.jboss.hal.ballroom.dataprovider;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import com.google.common.collect.Lists;
import com.google.common.primitives.Ints;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatcher;

import static com.google.common.primitives.Ints.asList;
import static java.lang.Integer.parseInt;
import static java.lang.System.arraycopy;
import static java.util.Comparator.naturalOrder;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;

@SuppressWarnings({"HardCodedStringLiteral", "DuplicateStringLiteralInspection", "unchecked"})
public class DataProviderTest {

    private static class ItemsMatcher implements ArgumentMatcher<Iterable<Integer>> {

        private final int[] expected;

        private ItemsMatcher(int[] expected) {this.expected = expected;}

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
        return toArray(Lists.newArrayList(iterable));
    }

    private static int[] toArray(List<Integer> list) {
        return Ints.toArray(list);
    }

    private static final int PAGE_SIZE = 10;
    private static final int[] EVEN = new int[]{0, 2, 4, 6, 8};
    private static final int[] BY_THREE = new int[]{0, 3, 6, 9};
    private static final int[] COMBINED = new int[]{0, 6};
    private static final Function<Integer, String> IDENTIFIER = String::valueOf;
    private static final Filter<Integer> DIVISIBLE = (number, filter) -> number % parseInt(filter) == 0;


    private DataProvider<Integer> single;
    private DataProvider<Integer> multi;
    private Display<Integer> display;
    private SelectHandler<Integer> selectHandler;

    @Before
    public void setUp() throws Exception {
        single = new DataProvider<>(IDENTIFIER, false, PAGE_SIZE);
        multi = new DataProvider<>(IDENTIFIER, true, PAGE_SIZE);

        display = mock(Display.class);
        single.addDisplay(display);
        multi.addDisplay(display);

        selectHandler = mock(SelectHandler.class);
        single.onSelect(selectHandler);
        multi.onSelect(selectHandler);
    }


    // ------------------------------------------------------ items

    @Test
    public void updateItems() throws Exception {
        int[] items = items(PAGE_SIZE);
        single.update(asList(items));
        assertVisibleFilteredAll(single, items, items, items);
    }

    @Test
    public void contains() throws Exception {
        int[] items = items(23);
        single.update(asList(items));
        assertTrue(single.contains(0));
        assertTrue(single.contains(9));
        assertTrue(single.contains(10));
        assertTrue(single.contains(22));
        assertFalse(single.contains(23));
    }

    @Test
    public void visible() throws Exception {
        int[] items = items(23);
        single.update(asList(items));
        assertTrue(single.isVisible(0));
        assertTrue(single.isVisible(9));
        assertFalse(single.isVisible(10));
        assertFalse(single.isVisible(22));
        assertFalse(single.isVisible(23));
    }


    // ------------------------------------------------------ page size

    @Test
    public void setPageSize() throws Exception {
        int[] items = items(PAGE_SIZE);
        single.update(asList(items));
        assertVisibleFilteredAll(single, items(PAGE_SIZE), items, items);
        verify(display).showItems(itemsMatcher(items(PAGE_SIZE)), eq(new PageInfo(PAGE_SIZE, 0, PAGE_SIZE, PAGE_SIZE)));

        single.setPageSize(6);
        assertVisibleFilteredAll(single, items(6), items, items);
        verify(display).showItems(itemsMatcher(items(6)), eq(new PageInfo(6, 0, 6, PAGE_SIZE)));
    }

    @Test
    public void getPageSize() throws Exception {
        single.update(asList(items(PAGE_SIZE)));
        assertEquals(1, single.getPageInfo().getPages());

        single.update(asList(items(PAGE_SIZE - 1)));
        assertEquals(1, single.getPageInfo().getPages());

        single.update(asList(items(PAGE_SIZE + 1)));
        assertEquals(2, single.getPageInfo().getPages());
    }


    // ------------------------------------------------------ paging

    @Test
    public void navigate() throws Exception {
        int[] items = items(42);
        single.update(asList(items));

        reset(display);
        single.gotoFirstPage();
        assertVisibleFilteredAll(single, items(PAGE_SIZE), items, items);
        verify(display, never()).showItems(any(), any());

        reset(display);
        single.gotoPreviousPage();
        assertVisibleFilteredAll(single, items(PAGE_SIZE), items, items);
        verify(display, never()).showItems(any(), any());

        reset(display);
        single.gotoNextPage();
        assertVisibleFilteredAll(single, items(10, 19), items, items);
        verify(display).showItems(itemsMatcher(items(10, 19)), eq(new PageInfo(PAGE_SIZE, 1, PAGE_SIZE, 42)));

        reset(display);
        single.gotoPage(2);
        assertVisibleFilteredAll(single, items(20, 29), items, items);
        verify(display).showItems(itemsMatcher(items(20, 29)), eq(new PageInfo(PAGE_SIZE, 2, 10, 42)));

        reset(display);
        single.gotoLastPage();
        assertVisibleFilteredAll(single, items(40, 41), items, items);
        verify(display).showItems(itemsMatcher(items(40, 41)), eq(new PageInfo(PAGE_SIZE, 4, 2, 42)));

        reset(display);
        single.gotoNextPage();
        assertVisibleFilteredAll(single, items(40, 41), items, items);
        verify(display, never()).showItems(any(), any());

        reset(display);
        single.gotoLastPage();
        assertVisibleFilteredAll(single, items(40, 41), items, items);
        verify(display, never()).showItems(any(), any());
    }


    // ------------------------------------------------------ select

    @Test
    public void singleSelect() throws Exception {
        single.update(asList(items(PAGE_SIZE)));

        reset(display, selectHandler);
        single.select(2, true);
        assertSelection(single, new int[]{2});
        verify(display).updateSelection(new SelectionInfo<>(IDENTIFIER, false, selection(new int[]{2})));
        verify(selectHandler).onSelect(2);

        reset(display, selectHandler);
        single.select(2, false);
        assertNoSelection(single);
        verify(display).updateSelection(new SelectionInfo<>(IDENTIFIER, false));
        verify(selectHandler, never()).onSelect(anyInt());
    }

    @Test
    public void multiSelect() throws Exception {
        multi.update(asList(items(PAGE_SIZE)));

        reset(display, selectHandler);
        multi.select(1, true);
        assertSelection(multi, new int[]{1});
        verify(display).updateSelection(new SelectionInfo<>(IDENTIFIER, true, selection(new int[]{1})));
        verify(selectHandler).onSelect(1);

        reset(display, selectHandler);
        multi.select(3, true);
        assertSelection(multi, new int[]{1, 3});
        verify(display).updateSelection(new SelectionInfo<>(IDENTIFIER, true, selection(new int[]{1, 3})));
        verify(selectHandler).onSelect(3);

        reset(display, selectHandler);
        multi.select(5, true);
        assertSelection(multi, new int[]{1, 3, 5});
        verify(display).updateSelection(new SelectionInfo<>(IDENTIFIER, true, selection(new int[]{1, 3, 5})));
        verify(selectHandler).onSelect(5);

        reset(display, selectHandler);
        multi.select(5, false);
        assertSelection(multi, new int[]{1, 3});
        verify(display).updateSelection(new SelectionInfo<>(IDENTIFIER, true, selection(new int[]{1, 3})));
        verify(selectHandler, never()).onSelect(anyInt());

        reset(display, selectHandler);
        multi.select(3, false);
        assertSelection(multi, new int[]{1});
        verify(display).updateSelection(new SelectionInfo<>(IDENTIFIER, true, selection(new int[]{1})));
        verify(selectHandler, never()).onSelect(anyInt());

        reset(display, selectHandler);
        multi.select(1, false);
        assertNoSelection(multi);
        verify(display).updateSelection(new SelectionInfo<>(IDENTIFIER, true));
        verify(selectHandler, never()).onSelect(anyInt());
    }

    @Test
    public void singleSelectAll() throws Exception {
        single.update(asList(items(PAGE_SIZE)));

        reset(display, selectHandler);
        single.selectAll();
        assertNoSelection(single);
        verify(display, never()).updateSelection(any());
        verify(selectHandler, never()).onSelect(anyInt());

        reset(display, selectHandler);
        single.clearAllSelection();
        assertNoSelection(single);
        verify(display, never()).updateSelection(any());
        verify(selectHandler, never()).onSelect(anyInt());
    }

    @Test
    public void singleSelectVisible() throws Exception {
        single.update(asList(items(PAGE_SIZE)));

        reset(display, selectHandler);
        single.selectVisible();
        assertNoSelection(single);
        verify(display, never()).updateSelection(any());
        verify(selectHandler, never()).onSelect(anyInt());

        reset(display, selectHandler);
        single.clearAllSelection();
        assertNoSelection(single);
        verify(display, never()).updateSelection(any());
        verify(selectHandler, never()).onSelect(anyInt());
    }

    @Test
    public void multiSelectAll() throws Exception {
        int[] items = items(PAGE_SIZE);
        multi.update(asList(items));

        reset(display, selectHandler);
        multi.selectAll();
        assertSelection(multi, items);
        verify(display).updateSelection(new SelectionInfo<>(IDENTIFIER, true, selection(items)));
        verify(selectHandler, never()).onSelect(anyInt());

        reset(display, selectHandler);
        multi.clearAllSelection();
        assertNoSelection(multi);
        verify(display).updateSelection(new SelectionInfo<>(IDENTIFIER, true));
        verify(selectHandler, never()).onSelect(anyInt());
    }

    @Test
    public void multiSelectVisible() throws Exception {
        int[] items = items(42);
        multi.update(asList(items));

        reset(display, selectHandler);
        multi.selectVisible();
        assertSelection(multi, items(PAGE_SIZE));
        verify(display).updateSelection(new SelectionInfo<>(IDENTIFIER, true, selection(items(PAGE_SIZE))));
        verify(selectHandler, never()).onSelect(anyInt());

        reset(display, selectHandler);
        multi.clearVisibleSelection();
        assertNoSelection(multi);
        verify(display).updateSelection(new SelectionInfo<>(IDENTIFIER, true));
        verify(selectHandler, never()).onSelect(anyInt());
    }

    @Test
    public void selectAndNavigate() throws Exception {
        int[] items = items(42);
        multi.update(asList(items));

        reset(display, selectHandler);
        multi.selectVisible();
        assertSelection(multi, items(PAGE_SIZE));
        verify(display).updateSelection(new SelectionInfo<>(IDENTIFIER, true, selection(items(PAGE_SIZE))));
        verify(selectHandler, never()).onSelect(anyInt());

        int[] selection = new int[12];
        arraycopy(items(PAGE_SIZE), 0, selection, 0, 10);
        arraycopy(items(40, 41), 0, selection, 10, 2);
        multi.gotoLastPage();

        reset(display, selectHandler);
        multi.selectVisible();
        assertSelection(multi, selection);
        verify(display).updateSelection(new SelectionInfo<>(IDENTIFIER, true, selection(selection)));
        verify(selectHandler, never()).onSelect(anyInt());
    }


    // ------------------------------------------------------ filter

    @Test
    public void filter() throws Exception {
        single.update(asList(items(PAGE_SIZE)));

        reset(display);
        single.addFilter("even", new FilterValue<>(DIVISIBLE, "2"));
        verify(display).showItems(itemsMatcher(EVEN), eq(new PageInfo(PAGE_SIZE, 0, 5, 5)));

        reset(display);
        single.addFilter("byThree", new FilterValue<>(DIVISIBLE, "3"));
        verify(display).showItems(itemsMatcher(COMBINED), eq(new PageInfo(PAGE_SIZE, 0, 2, 2)));

        reset(display);
        single.removeFilter("even");
        verify(display).showItems(itemsMatcher(BY_THREE), eq(new PageInfo(PAGE_SIZE, 0, 4, 4)));

        reset(display);
        single.clearFilters();
        verify(display).showItems(itemsMatcher(items(PAGE_SIZE)), eq(new PageInfo(PAGE_SIZE, 0, PAGE_SIZE, PAGE_SIZE)));
    }

    @Test
    public void changeFilter() throws Exception {
        single.update(asList(items(PAGE_SIZE)));

        reset(display);
        single.addFilter("divisible", new FilterValue<>(DIVISIBLE, "2"));
        verify(display).showItems(itemsMatcher(EVEN), eq(new PageInfo(PAGE_SIZE, 0, 5, 5)));

        reset(display);
        single.addFilter("divisible", new FilterValue<>(DIVISIBLE, "3"));
        verify(display).showItems(itemsMatcher(BY_THREE), eq(new PageInfo(PAGE_SIZE, 0, 4, 4)));
    }

    @Test
    public void removeUnknownFilter() throws Exception {
        single.update(asList(items(PAGE_SIZE)));

        reset(display);
        single.removeFilter("foo");
        verify(display, never()).showItems(any(), any());
    }

    @Test
    public void clearNoFilter() throws Exception {
        single.update(asList(items(PAGE_SIZE)));

        reset(display);
        single.clearFilters();
        verify(display, never()).showItems(any(), any());
    }

    @Test
    public void getFilter() throws Exception {
        assertFalse(single.hasFilters());
        assertSame(FilterValue.EMPTY, single.getFilter("foo"));
    }

    // ------------------------------------------------------ sort

    @Test
    public void sortAsc() throws Exception {
        int[] items = {0, 8, 1, 5};
        int[] sorted = {0, 1, 5, 8};

        single.update(asList(items));
        verify(display).showItems(itemsMatcher(items), eq(new PageInfo(PAGE_SIZE, 0, 4, 4)));
        single.setComparator(naturalOrder());
        verify(display).showItems(itemsMatcher(sorted), eq(new PageInfo(PAGE_SIZE, 0, 4, 4)));
    }

    @Test
    public void sortDesc() throws Exception {
        int[] items = {0, 8, 1, 5};
        int[] sorted = {8, 5, 1, 0};

        single.update(asList(items));
        verify(display).showItems(itemsMatcher(items), eq(new PageInfo(PAGE_SIZE, 0, 4, 4)));
        single.setComparator(Comparator.<Integer>naturalOrder().reversed());
        verify(display).showItems(itemsMatcher(sorted), eq(new PageInfo(PAGE_SIZE, 0, 4, 4)));
    }


    // ------------------------------------------------------ helper methods

    private void assertVisibleFilteredAll(DataProvider<Integer> dp, int[] visible, int[] filtered, int[] all) {
        assertArrayEquals(all, toArray(dp.getAllItems()));
        assertArrayEquals(filtered, toArray(dp.getFilteredItems()));
        assertArrayEquals(visible, toArray(dp.getVisibleItems()));
    }

    private void assertNoSelection(DataProvider<Integer> dp) {
        assertFalse(dp.getSelectionInfo().hasSelection());
        assertTrue(dp.getSelectionInfo().getSelection().isEmpty());
        assertNull(dp.getSelectionInfo().getSingleSelection());
    }

    private void assertSelection(DataProvider<Integer> dp, int[] selection) {
        assertTrue(dp.getSelectionInfo().hasSelection());
        assertFalse(dp.getSelectionInfo().getSelection().isEmpty());
        assertTrue(new HashSet<>(Ints.asList(selection)).contains(dp.getSelectionInfo().getSingleSelection()));
        int[] dpSelection = toArray(dp.getSelectionInfo().getSelection());
        Arrays.sort(dpSelection);
        assertArrayEquals(selection, dpSelection);
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

    private Map<String, Integer> selection(int[] items) {
        Map<String, Integer> selection = new HashMap<>();
        for (int item : items) {
            selection.put(String.valueOf(item), item);
        }
        return selection;
    }
}