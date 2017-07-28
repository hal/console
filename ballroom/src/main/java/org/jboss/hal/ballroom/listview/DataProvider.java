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
package org.jboss.hal.ballroom.listview;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

/**
 * Holds data for displays like {@link ListView}. Changes to the data provider are reflected in the connected
 * displays.
 */
public class DataProvider<T> {

    private final Function<T, String> identifier;
    private final Map<String, T> allItems;
    private final List<Display<T>> displays;
    private Map<String, T> visibleItems;
    private List<FilterValue<T>> filterValues;
    private Comparator<T> comparator;
    private boolean asc;

    public DataProvider(Function<T, String> identifier) {
        this.identifier = identifier;
        this.allItems = new LinkedHashMap<>();
        this.displays = new ArrayList<>();
        this.visibleItems = new LinkedHashMap<>();
        this.filterValues = new ArrayList<>();
    }

    public void addDisplay(Display<T> display) {
        displays.add(display);
    }

    public void update(Iterable<T> items) {
        allItems.clear();
        for (T item : items) {
            String id = identifier.apply(item);
            allItems.put(id, item);
        }
        apply(filterValues, comparator, asc);
    }

    public void apply(List<FilterValue<T>> filter, Comparator<T> comparator, boolean asc) {
        this.filterValues = filter;
        this.comparator = comparator;
        this.asc = asc;

        if (filterValues.isEmpty()) {
            if (comparator != null) {
                visibleItems = allItems.values().stream()
                        .sorted(asc ? comparator : comparator.reversed())
                        .collect(toMap(identifier, identity()));
            } else {
                visibleItems = allItems;
            }

        } else {
            Predicate<T> predicate = null;
            for (FilterValue<T> filterValue : filter) {
                if (predicate == null) {
                    predicate = i -> filterValue.getFilter().test(i, filterValue.getValue());
                } else {
                    predicate = predicate.and(i -> filterValue.getFilter().test(i, filterValue.getValue()));
                }
            }
            Stream<T> stream = allItems.values().stream().filter(predicate);
            if (comparator != null) {
                stream = stream.sorted(asc ? comparator : comparator.reversed());
            }
            visibleItems = stream.collect(toMap(identifier, identity()));
        }

        updateDisplays();
    }

    public boolean contains(T item) {
        return allItems.containsKey(identifier.apply(item));
    }

    public boolean isVisible(T item) {
        return visibleItems.containsKey(identifier.apply(item));
    }

    public Iterable<T> getAllItems() {
        return allItems.values();
    }

    public Iterable<T> getVisibleItems() {
        return visibleItems.values();
    }

    private void updateDisplays() {
        int visible = visibleItems.size();
        int total = allItems.size();
        for (Display<T> display : displays) {
            display.showItems(visibleItems.values(), visible, total);
        }
    }
}
