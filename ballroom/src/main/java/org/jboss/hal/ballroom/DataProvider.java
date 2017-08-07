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
package org.jboss.hal.ballroom;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import com.google.common.collect.Lists;
import org.jboss.hal.ballroom.listview.ListView;
import org.jboss.hal.config.Settings;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.jboss.hal.config.Settings.DEFAULT_PAGE_SIZE;
import static org.jboss.hal.config.Settings.Key.PAGE_SIZE;

/** Holds state for displays like {@link ListView}. Changes to the state is reflected in the connected displays. */
public class DataProvider<T> {

    private final Function<T, String> identifier;
    private final Map<String, T> allItems;
    private Map<String, T> visibleItems;
    private final Map<String, FilterValue<T>> filterValues;
    private final List<Display<T>> displays;
    private Comparator<T> comparator;
    private int filtered;
    private int page;
    private int pageSize;

    public DataProvider(Function<T, String> identifier) {
        this.identifier = identifier;
        this.allItems = new LinkedHashMap<>();
        this.visibleItems = new LinkedHashMap<>();
        this.filterValues = new HashMap<>();
        this.displays = new ArrayList<>();
        this.filtered = 0;
        this.page = 0;
        this.pageSize = Settings.INSTANCE.get(PAGE_SIZE).asInt(DEFAULT_PAGE_SIZE);
    }

    public void addDisplay(Display<T> display) {
        displays.add(display);
    }

    /** Replaces the items and applies the current filter, sort order and paging. */
    public void update(Iterable<T> items) {
        allItems.clear();
        for (T item : items) {
            String id = identifier.apply(item);
            allItems.put(id, item);
        }
        update();
    }

    /** Applies the filter, sort order and paging to the current items. */
    public void update() {
        Stream<T> stream = allItems.values().stream();
        if (!filterValues.isEmpty()) {
            Predicate<T> predicate = null;
            for (FilterValue<T> filterValue : filterValues.values()) {
                if (predicate == null) {
                    predicate = i -> filterValue.getFilter().test(i, filterValue.getValue());
                } else {
                    predicate = predicate.and(i -> filterValue.getFilter().test(i, filterValue.getValue()));
                }
            }
            stream = stream.filter(predicate);
        }
        if (comparator != null) {
            stream = stream.sorted(comparator);
        }
        List<T> values = stream.collect(toList());
        filtered = values.size();
        if (filtered > pageSize) {
            values = paged(values);
        }
        visibleItems = values.stream().collect(toMap(identifier, identity()));
        updateDisplays(visibleItems.size(), filtered);
    }

    private List<T> paged(List<T> values) {
        List<List<T>> pages = Lists.partition(values, pageSize);
        return pages.get(min(page, pages.size() - 1));
    }

    public boolean contains(T item) {
        return allItems.containsKey(identifier.apply(item));
    }

    public boolean isVisible(T item) {
        return visibleItems.containsKey(identifier.apply(item));
    }

    public void addFilter(String name, FilterValue<T> filter) {
        filterValues.put(name, filter);
    }

    @SuppressWarnings("unchecked")
    public FilterValue<T> getFilter(String name) {
        return filterValues.getOrDefault(name, FilterValue.EMPTY);
    }

    public boolean hasFilters() {
        return !filterValues.isEmpty();
    }

    public void removeFilter(String name) {
        filterValues.remove(name);
    }

    public void clearFilters() {
        filterValues.clear();
    }

    public void setComparator(Comparator<T> comparator) {
        this.comparator = comparator;
    }

    public Comparator<T> getComparator() {
        return comparator;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = max(1, pageSize);
    }

    public int getPage() {
        return page;
    }

    public int getPages() {
        int pages = filtered / pageSize;
        if (filtered % pageSize != 0) {
            pages++;
        }
        return max(1, pages);
    }

    public void setPage(int page) {
        int safePage = max(0, page);
        this.page = min(safePage, getPages() - 1);
    }

    private void updateDisplays(int visible, int total) {
        PageInfo pageInfo = new PageInfo(page, pageSize, visible, total);
        for (Display<T> display : displays) {
            display.showItems(visibleItems.values(), pageInfo);
        }
    }
}
