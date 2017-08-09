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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collector;
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
    private final boolean multiselect;
    private final Map<String, T> allItems;
    private final Map<String, FilterValue<T>> filterValues;
    private final Set<String> selection;
    private final List<Display<T>> displays;
    private Map<String, T> filteredItems;
    private Map<String, T> visibleItems;
    private List<SelectHandler<T>> selectHandler;
    private Comparator<T> comparator;
    private int pageSize;
    private int page;

    public DataProvider(Function<T, String> identifier, boolean multiselect) {
        this(identifier, multiselect, Settings.INSTANCE.get(PAGE_SIZE).asInt(DEFAULT_PAGE_SIZE));
    }

    DataProvider(Function<T, String> identifier, boolean multiselect, int pageSize) {
        this.identifier = identifier;
        this.multiselect = multiselect;
        this.allItems = new LinkedHashMap<>();
        this.filteredItems = new LinkedHashMap<>();
        this.visibleItems = new LinkedHashMap<>();
        this.filterValues = new HashMap<>();
        this.selection = new HashSet<>();
        this.selectHandler = new ArrayList<>();
        this.displays = new ArrayList<>();
        this.pageSize = pageSize;
        this.page = 0;
    }


    // ------------------------------------------------------ update items

    /** Replaces the items and applies the current filter, sort order and paging. */
    public void update(Iterable<T> items) {
        allItems.clear();
        selection.clear();
        for (T item : items) {
            allItems.put(getId(item), item);
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
        if (values.size() > pageSize) {
            filteredItems = values.stream().collect(toLinkedMap(identifier, identity()));
            values = paged(values);
            visibleItems = values.stream().collect(toLinkedMap(identifier, identity()));
        } else {
            filteredItems = visibleItems = values.stream().collect(toLinkedMap(identifier, identity()));
        }
        showItems();
        updateSelection();
    }

    private Collector<T, ?, Map<String, T>> toLinkedMap(Function<? super T, ? extends String> keyMapper,
            Function<? super T, ? extends T> valueMapper) {
        return toMap(keyMapper, valueMapper,
                (u, v) -> {
                    throw new IllegalStateException(String.format("Duplicate key %s", u)); //NON-NLS
                },
                LinkedHashMap::new);
    }

    public boolean contains(T item) {
        return allItems.containsKey(identifier.apply(item));
    }

    public boolean isVisible(T item) {
        return visibleItems.containsKey(identifier.apply(item));
    }

    public String getId(T item) {
        return identifier.apply(item);
    }

    public Iterable<T> getAllItems() {
        return allItems.values();
    }

    public Iterable<T> getFilteredItems() {
        return filteredItems.values();
    }

    public Iterable<T> getVisibleItems() {
        return visibleItems.values();
    }


    // ------------------------------------------------------ selection

    public void onSelect(SelectHandler<T> selectHandler) {
        this.selectHandler.add(selectHandler);
    }

    /** Selects all items if {@ocde multiselect == true}. Does not fire selection events */
    public void selectAll() {
        if (multiselect) {
            for (String id : filteredItems.keySet()) {
                selectInternal(id, true);
            }
            updateSelection();
        }
    }

    /** Selects the visible items if {@ocde multiselect == true}. Does not fire selection events */
    public void selectVisible() {
        if (multiselect) {
            for (String id : visibleItems.keySet()) {
                selectInternal(id, true);
            }
            updateSelection();
        }
    }

    /** Clears the selection for all items */
    public void clearAllSelection() {
        for (String id : filteredItems.keySet()) {
            selectInternal(id, false);
        }
        updateSelection();
    }

    /** Clears the selection for the visible items */
    public void clearVisibleSelection() {
        for (String id : visibleItems.keySet()) {
            selectInternal(id, false);
        }
        updateSelection();
    }

    /** Selects the specified item and fires a selection event */
    public void select(T item, boolean select) {
        if (select && !multiselect) {
            for (String id : visibleItems.keySet()) {
                selectInternal(id, false);
            }
        }
        selectInternal(getId(item), select);
        fireSelection(item);
        updateSelection();
    }

    private void selectInternal(String id, boolean select) {
        if (select) {
            selection.add(id);
        } else {
            selection.remove(id);
        }
    }

    private void fireSelection(T item) {
        for (SelectHandler<T> handler : selectHandler) {
            handler.onSelect(item);
        }
    }

    public boolean isSelected(T item) {
        return isSelected(getId(item));
    }

    private boolean isSelected(String id) {
        return selection.contains(id);
    }

    public boolean hasSelection() {
        return !selection.isEmpty();
    }

    public T getSingleSelection() {
        List<T> s = getSelection();
        if (!selection.isEmpty()) {
            return s.get(0);
        }
        return null;
    }

    public List<T> getSelection() {
        return selection.stream()
                .map(filteredItems::get)
                .filter(Objects::nonNull)
                .collect(toList());
    }


    // ------------------------------------------------------ filter

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


    // ------------------------------------------------------ sort

    public void setComparator(Comparator<T> comparator) {
        this.comparator = comparator;
    }

    public Comparator<T> getComparator() {
        return comparator;
    }


    // ------------------------------------------------------ paging

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
        int total = filteredItems.size();
        int pages = total / pageSize;
        if (total % pageSize != 0) {
            pages++;
        }
        return max(1, pages);
    }

    public void setPage(int page) {
        int safePage = max(0, page);
        this.page = min(safePage, getPages() - 1);
    }

    private List<T> paged(List<T> values) {
        List<List<T>> pages = Lists.partition(values, pageSize);
        return pages.get(min(page, pages.size() - 1));
    }


    // ------------------------------------------------------ displays

    public void addDisplay(Display<T> display) {
        displays.add(display);
    }

    private void showItems() {
        PageInfo pageInfo = new PageInfo(page, pageSize, visibleItems.size(), filteredItems.size());
        for (Display<T> display : displays) {
            display.showItems(visibleItems.values(), pageInfo);
        }
    }

    private void updateSelection() {
        Map<String, T> selectedItems = selection.stream().collect(toMap(identity(), id -> filteredItems.get(id)));
        Selection<T> sel = new Selection<>(identifier, selectedItems, multiselect, filteredItems.size());
        for (Display<T> display : displays) {
            display.updateSelection(sel);
        }
    }
}
