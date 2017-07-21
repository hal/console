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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

/**
 * Holds data for displays like {@link ListView}. Changes to the data provider are reflected in the connected displays.
 */
public class DataProvider<T> {

    private final Function<T, String> identifier;
    private final Map<String, T> allItems;
    private final List<ListView<T>> displays;
    private Map<String, T> visibleItems;
    private List<FilterValue<T>> filterValues;

    public DataProvider(Function<T, String> identifier) {
        this.identifier = identifier;
        this.allItems = new LinkedHashMap<>();
        this.displays = new ArrayList<>();
        this.visibleItems = new LinkedHashMap<>();
        this.filterValues = new ArrayList<>();
    }

    public void addDisplay(ListView<T> listView) {
        listView.setDataProvider(this);
        displays.add(listView);
    }

    public void setItems(Iterable<T> items) {
        allItems.clear();
        for (T item : items) {
            String id = identifier.apply(item);
            allItems.put(id, item);
        }
        apply(filterValues);
    }

    public void apply(List<FilterValue<T>> filter) {
        this.filterValues = filter;

        if (filterValues.isEmpty()) {
            visibleItems.clear();
            visibleItems.putAll(allItems);

        } else {
            Predicate<T> predicate = null;
            for (FilterValue<T> filterValue : filter) {
                if (predicate == null) {
                    predicate = i -> filterValue.getFilter().test(i, filterValue.getValue());
                } else {
                    predicate = predicate.and(i -> filterValue.getFilter().test(i, filterValue.getValue()));
                }
            }
            visibleItems = allItems.values().stream()
                    .filter(predicate)
                    .collect(toMap(identifier, identity()));
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

    protected Function<T, String> identifer() {
        return identifier;
    }

    private void updateDisplays() {
        for (ListView<T> display : displays) {
            display.setItems(visibleItems.values());
        }
    }
}
